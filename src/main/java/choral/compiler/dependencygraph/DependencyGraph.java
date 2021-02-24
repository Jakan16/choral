package choral.compiler.dependencygraph;

import choral.ast.CompilationUnit;
import choral.ast.ImportDeclaration;
import choral.ast.Name;
import choral.ast.Node;
import choral.ast.body.Class;
import choral.ast.body.Enum;
import choral.ast.body.*;
import choral.ast.expression.*;
import choral.ast.statement.*;
import choral.ast.type.FormalTypeParameter;
import choral.ast.type.FormalWorldParameter;
import choral.ast.type.TypeExpression;
import choral.ast.type.WorldArgument;
import choral.ast.visitors.ChoralVisitorInterface;
import choral.compiler.dependencygraph.dnodes.*;
import choral.compiler.dependencygraph.role.Role;
import choral.compiler.dependencygraph.role.TemporaryRole;
import choral.compiler.dependencygraph.symboltable.PackageHandler;
import choral.compiler.dependencygraph.symboltable.SymbolTable;
import choral.compiler.dependencygraph.symboltable.Template;
import choral.utils.Pair;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Creates a graph for the dependencies required to complete an expression.
 */
public class DependencyGraph implements ChoralVisitorInterface< DNode > {

	// used to resolve identifiers
	private final Context context;

	public DependencyGraph( Collection< CompilationUnit > cus,
							Collection< CompilationUnit > headerUnits
	) {
		this.context = new Context( cus, headerUnits );
	}

	/**
	 * Creates a dependency graph for a set of {@link CompilationUnit}'s
	 * @param cus The units of the source code
	 * @param headerUnits The units from header files
	 * @return The AST with missing coms and roles inserted
	 */
	public static Collection< CompilationUnit > walk(
			Collection< CompilationUnit > cus,
			Collection< CompilationUnit > headerUnits
	){
		DRoot root = DRoot.emptyRoot();
		DependencyGraph visitor = new DependencyGraph( cus, headerUnits );
		// walk all source nodes
		for( CompilationUnit cu: cus ) {
			root = root.merge( visitor.visit( cu ) );
		}

		GraphSolver.solve( root );

		//System.out.println( DependencyGraphPrinter.walk( root ) );
		return ComInjector.inject( cus );
		/*System.out.println( DependencyGraphPrinter.walk( root ) );

		var injected = ComInjector.inject( cus );
		for( var cu: injected ){
			System.out.println(new PrettyPrinterVisitor().visit( cu ) );
		}

		return injected; */
	}

	@Override
	public DNode visit( CompilationUnit n ) {
		DRoot nodes = DRoot.emptyRoot();
		for( Class c : n.classes() ) {
			var tem = this.context.getTemplate( n.packageDeclaration(), c );
			this.context.pushFrame( tem );
			var dClass = new DClass( tem, visit( c ) );
			dClass.setPosition( c.position() );
			nodes = nodes.merge( dClass );
			this.context.popFrame();
		}
		return nodes;
	}

	@Override
	public DNode visit( ImportDeclaration n ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public DNode visit( Class n ) {
		DRoot nodes = DRoot.emptyRoot();
		ContextFrame frame = context.currentFrame();
		for( ClassMethodDefinition definition: n.methods() ){
			// the context frame stack may change during a visit to a method,
			// but should return to the original state when the visit finishes.
			assert frame == context.currentFrame();
			var methodSig = context.currentTem().getMethodSig( definition );
			frame.setCurrentMethod( methodSig );
			DNode classRoot = definition.accept( this );
			var dMethod = new DMethod( methodSig.getName(), classRoot );
			dMethod.setPosition( definition.position() );
			nodes = nodes.merge( dMethod );
		}
		frame.clearCurrentMethod();

		for( ConstructorDefinition definition: n.constructors() ){
			var dMethod = new DMethod( "constructor", definition.accept( this ) );
			dMethod.setPosition( definition.position() );
			nodes = nodes.merge( dMethod );
		}

		return nodes;
	}

	@Override
	public DNode visit( Enum n ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public DNode visit( Interface n ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public DNode visit( Statement n ) {
		return n.accept( this );
	}

	@Override
	public DNode visit( BlockStatement n ) {
		context.enterScope();
		DNode nodes = n.enclosedStatement().accept( this );
		context.exitScope();
		nodes = nodes.merge( n.continuation().accept( this ) );
		return nodes;
	}

	@Override
	public DNode visit( SelectStatement n ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public DNode visit( ScopedExpression n ) {
		DNode scope = n.scope().accept( this );
		context.pushContextOf( scope ); // pushes context of the scope
		DNode scopedExp = n.scopedExpression().accept( this );
		context.popFrame();
		var node = new DExpression( Arrays.asList( scope, scopedExp ), "ScopedExpression" );
		node.setPosition( n.position() );
		n.setDependencies( node );
		if( scope instanceof DThis ){
			// This and Super has no role information,
			// and the graph can be simplified by omitting these.
			return scopedExp;
		}
		return node;
	}

	@Override
	public DNode visit( ExpressionStatement n ) {
		DNode exp = n.expression().accept( this );
		return exp.merge( n.continuation().accept( this ) );
	}

	@Override
	public DNode visit( IfStatement n ) {
		var guard = n.condition().accept( this );
		context.enterScope();
		var ifBranch = n.ifBranch().accept( this );
		context.exitScope();
		context.enterScope();
		var elseBranch = n.elseBranch().accept( this );
		context.exitScope();
		var continuation = n.continuation().accept( this );
		return guard.merge( ifBranch ).merge( elseBranch ).merge( continuation );
	}

	@Override
	public DNode visit( SwitchStatement n ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public DNode visit( TryCatchStatement n ) {
		context.enterScope();
		DNode dNode = n.body().accept( this );
		context.exitScope();
		for( Pair<VariableDeclaration, Statement> pair: n.catches() ){
			context.enterScope();
			dNode = dNode.merge( pair.left().accept( this ) ).merge( pair.right().accept( this ) );
			context.exitScope();
		}
		return dNode.merge( n.continuation().accept( this ) );
	}

	@Override
	public DNode visit( NilStatement n ) {
		return DRoot.emptyRoot();
	}

	@Override
	public DNode visit( ReturnStatement n ) {
		DNode returnExp = null;
		if( n.returnExpression() != null ) {
			returnExp = n.returnExpression().accept( this );
		}
		// create explicit return node, as the return may be required to be at specific roles
		DNode returnNode = new DReturn( returnExp, context.currentFrame().getReturnType() );
		returnNode.setPosition( n.position() );
		n.setDependencies( returnNode );
		DNode continuation = n.continuation().accept( this );
		return returnNode.merge( continuation );
	}

	@Override
	public DNode visit( Expression n ) {
		return n.accept( this );
	}

	@Override
	public DNode visit( AssignExpression n ) {
		var target = n.target().accept( this );
		var value = n.value().accept( this );
		var node = new DAssign( target, value );
		node.setPosition( n.position() );
		n.setDependencies( node );
		return node;
	}

	@Override
	public DNode visit( BinaryExpression n ) {
		var left =  n.left().accept( this );
		var right =  n.right().accept( this );
		DType resultType = getTypeOfOperation( left.getType(), right.getType(), n.operator() );
		var node = new DBinaryExpression( left, right, n.operator(), resultType );
		node.setPosition( n.position() );
		n.setDependencies( node );
		return node;
	}

	private DType getTypeOfOperation( DType left, DType right, BinaryExpression.Operator operator ) {
		return switch( operator ){
			case SHORT_CIRCUITED_OR,
				SHORT_CIRCUITED_AND,
				EQUALS,
				NOT_EQUALS,
				LESS,
				GREATER,
				LESS_EQUALS,
				GREATER_EQUALS ->
					context.getTypeFromName( "boolean", new TemporaryRole() );
			case OR,
				AND -> context.getTypeFromName( "int", new TemporaryRole() );
			case PLUS -> context.getTypeFromName( left.getTem().getName().equals( "String" ) ||
							right.getTem().getName().equals( "String" ) ? "String" : (left.getTem().getName().equals( "double" ) ||
							right.getTem().getName().equals( "double" ) ? "double" : "int"),
					new TemporaryRole() );
			case MINUS,
				MULTIPLY,
				DIVIDE,
				REMAINDER ->
					context.getTypeFromName( left.getTem().getName().equals( "double" ) ||
							right.getTem().getName().equals( "double" ) ? "double" : "int",
							new TemporaryRole() );
		};
	}

	@Override
	public DNode visit( EnumCaseInstantiationExpression n ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public DNode visit( EnclosedExpression n ) {
		var node = new DExpression( Collections.singletonList( n.nestedExpression().accept( this ) ), "EnclosedExpression" );
		node.setPosition( n.position() );
		n.setDependencies( node );
		return node;
	}

	@Override
	public DNode visit( FieldAccessExpression n ) {
		DNode node;
		try {
			node = context.resolveIdentifier( n.name().identifier() );
		}catch( IllegalStateException e ){
			// if not variable or field, it may be an static access
			Template tem = context.currentFrame().resolveIdentifier( n.name().identifier() );
			if( tem == null || tem == Template.unknownTemplate() ){
				throw e;
			}
			node = new DStaticAccess(
					new DType(
							tem,
							Mapper.map( tem.worldParameters(), (t) -> new TemporaryRole() ),
							Collections.emptyList() ) );
		}
		n.setDependencies( node );
		return node;
	}

	@Override
	public DNode visit( StaticAccessExpression n ) {
		var node = new DStaticAccess( context.getTypeOfExpression( n.typeExpression() ) );
		node.setPosition( n.position() );
		n.setDependencies( node );
		return node;
	}

	@Override
	public DNode visit( MethodCallExpression n ) {
		Template.MethodSig sig = context.currentTem().getMethodSig( n );
		DType mrt = context.mapType( sig.getReturnType() );

		// return may have the type of a generic given by the function,
		// if this is the case the instance of the generic must be used.

		Map< Template, DType > genericMap = Mapper.mapping( sig.getTypeParameters(),
				n.typeArguments(),
				Mapper.id(),
				context::getTypeOfExpression );

		mrt = replaceClassGeneric( replaceMethodGeneric( mrt, genericMap ) );

		List< DNode > arguments = new ArrayList<>();

		for( Expression exp: n.arguments() ){
			// Each argument must be resolved in the context of the currently processed class
			// and not the class of the method.
			this.context.pushRootFrame();
			arguments.add( exp.accept( this ) );
			this.context.popFrame();
		}

		var node = new DMethodCall( sig.getName(), arguments, mrt,
				Mapper.map( sig.getParameters(), context::mapType ) );
		node.setPosition( n.position() );
		n.setDependencies( node );
		return node;
	}

	private DType replaceClassGeneric( DType type ){
		DType replaceType = context.currentFrame().replaceGeneric( type.getName() );
		if( replaceType != null ){
			return new DType( replaceType.getTem(), type.getRoles(), replaceType.getTypeArguments() );
		}
		return new DType( type.getTem(), type.getRoles(),
				Mapper.map( type.getTypeArguments(), this::replaceClassGeneric ) );
	}

	private DType replaceMethodGeneric( DType type, Map< Template, DType > genericMap ){
		DType replaceType = genericMap.get( type.getTem() );
		if( replaceType != null ){
			return new DType( replaceType.getTem(), type.getRoles(), replaceType.getTypeArguments() );
		}
		return new DType( type.getTem(), type.getRoles(),
				Mapper.map( type.getTypeArguments(), t -> replaceMethodGeneric( t, genericMap ) ) );
	}

	@Override
	public DNode visit( ClassInstantiationExpression n ) {
		List< DNode > dependencies = new ArrayList<>();

		// Each argument must be resolved in the context of the currently processed class
		// and not the class of the constructor.
		for( Expression exp: n.arguments() ){
			this.context.pushRootFrame();
			dependencies.add( exp.accept( this ) );
			this.context.popFrame();
		}

		DType type = context.getTypeOfExpression( n.typeExpression() );
		// get the used constructor for the instantiation
		// to get the destination roles of the arguments.
		Template.MethodSig sig = type.getTem().getConstructorSig( n.arguments().size() );
		var roleMap = Mapper.mapping(
				type.getTem().worldParameters(), type.getRoles() );
		List< DType > parameters = Mapper.map( sig.getParameters(),
				p -> new DType( p.getTem(),
						Mapper.map( p.getRoles(), roleMap::get ),
						p.getTypeArguments() ) );
		var node = new DClassInstantiation( dependencies, type.getName(), type, parameters );
		node.setPosition( n.position() );
		n.setDependencies( node );
		return node;
	}

	@Override
	public DNode visit( Name n ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public DNode visit( NotExpression n ) {
		var node = new DExpression( Collections.singletonList( n.expression().accept( this ) ), "NotExpression" );
		node.setPosition( n.position() );
		n.setDependencies( node );
		return node;
	}

	@Override
	public DNode visit( ThisExpression n ) {
		var node = new DThis();
		node.setPosition( n.position() );
		return node;
	}

	@Override
	public DNode visit( SuperExpression n ) {
		var node = new DThis();
		node.setPosition( n.position() );
		return node;
	}

	@Override
	public DNode visit( NullExpression n ) {
		Template nullTemplate = context.currentFrame().resolveIdentifier( "null" );
		DType type = new DType( nullTemplate, context.mapWorldToString( n.worlds() ), Collections.emptyList() );
		var node = new DLiteral( type );
		node.setPosition( n.position() );
		n.setDependencies( node );
		return node;
	}

	@Override
	public DNode visit( VariableDeclarationStatement n ) {
		List< DNode > nodes = visitAndCollect( n.variables() );
		DNode root = nodes.stream().reduce( DRoot.emptyRoot(), DNode::merge );
		return root.merge( n.continuation().accept( this ) );
	}

	@Override
	public DNode visit( BlankExpression n ) {
		return DRoot.emptyRoot();
	}

	@Override
	public DNode visit( LiteralExpression.BooleanLiteralExpression n ) {
		DType type = context.getTypeFromName( "boolean", context.rootFrame().roleFromName(n.world().name().identifier() ) );
		var node = new DLiteral( type );
		node.setPosition( n.position() );
		n.setDependencies( node );
		return node;
	}

	@Override
	public DNode visit( LiteralExpression.DoubleLiteralExpression n ) {
		DType type = context.getTypeFromName( "double", context.rootFrame().roleFromName(n.world().name().identifier() ) );
		var node = new DLiteral( type );
		node.setPosition( n.position() );
		n.setDependencies( node );
		return node;
	}

	@Override
	public DNode visit( LiteralExpression.IntegerLiteralExpression n ) {
		DType type = context.getTypeFromName( "int", context.rootFrame().roleFromName(n.world().name().identifier() ) );
		var node = new DLiteral( type );
		node.setPosition( n.position() );
		n.setDependencies( node );
		return node;
	}

	@Override
	public DNode visit( LiteralExpression.StringLiteralExpression n ) {
		DType type = context.getTypeFromName( "String", context.rootFrame().roleFromName(n.world().name().identifier() ) );
		var node = new DLiteral( type );
		node.setPosition( n.position() );
		n.setDependencies( node );
		return node;
	}

	@Override
	public DNode visit( SwitchArgument< ? > n ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public DNode visit( CaseSignature n ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public DNode visit( Field n ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public DNode visit( FormalMethodParameter n ) {
		context.addSymbol( n.name().identifier(), context.getTypeOfExpression( n.type() ) );
		return DRoot.emptyRoot();
	}

	@Override
	public DNode visit( ClassMethodDefinition n ) {
		context.enterScope();
		// The signature must be visited to add its symbols to the symbol table,
		// the result is not needed for the graph.
		n.signature().accept( this );
		DNode root = n.body().map( this::visit ).orElse( DRoot.emptyRoot() );
		context.exitScope();
		return root;
	}

	@Override
	public DNode visit( InterfaceMethodDefinition n ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public DNode visit( MethodSignature n ) {
		n.parameters().forEach( p -> p.accept( this ) );
		return DRoot.emptyRoot();
	}

	@Override
	public DNode visit( ConstructorDefinition n ) {
		context.enterScope();
		// The signature must be visited to add its symbols to the symbol table,
		// the result is not needed for the graph.
		n.signature().accept( this );
		DNode root = n.body().accept( this );
		context.exitScope();
		return root;
	}

	@Override
	public DNode visit( ConstructorSignature n ) {
		n.parameters().forEach( p -> p.accept( this ) );
		return DRoot.emptyRoot();
	}

	@Override
	public DNode visit( VariableDeclaration n ) {
		// Not needed for the final graph,
		// but used for resolving identifiers.
		DVariable variableNode = context.addSymbol( n.name().identifier(),
				context.getTypeOfExpression( n.type() ) );
		n.setDependencies( variableNode );
		return DRoot.emptyRoot();
	}

	@Override
	public DNode visit( TypeExpression n ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public DNode visit( WorldArgument n ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public DNode visit( FormalTypeParameter n ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public DNode visit( FormalWorldParameter n ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public DNode visit( Annotation n ) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Represent the context of a class, and it's generics and currently processed method.
	 * Also does mapping of roles from a foreign class into the world of its container.
	 */
	private static class ContextFrame {
		private final Template tem;
		private final Map< Role, Role > roleMap;
		private final Map< String, DType > genericMap;
		private Template.MethodSig methodSig;
		private Map< String, DType > funcGenericMap = Collections.emptyMap();

		/**
		 * Creates a new context for the given {@link Template}.
		 * Roles are mapped to themselves, and should only be used for the root frame.
		 * @param tem The {@link Template} to create a context over.
		 */
		public ContextFrame( Template tem ) {
			assert tem != null;
			this.tem = tem;

			// map roles to themselves
			roleMap = Mapper.idMap(	tem.worldParameters() );
			// The actual type of the generics cannot be know at this time.
			genericMap = Collections.emptyMap();
		}
		/**
		 * Creates a new context for the given {@link DType}.
		 * <br><br>
		 * Roles are mapped from the world of the {@link Template}
		 * of the {@link DType} to the world of the given {@link DType}.
		 * <br>
		 * The type parameters are mapped to the type arguments of the given {@link DType}.
		 * @param t The type to create a context over.
		 */
		public ContextFrame( DType t ) {
			Template tem = t.getTem();
			assert tem != null;
			this.tem = tem;

			roleMap = Mapper.mapping( tem.worldParameters(), t.getRoles() );
			if( t.getTypeArguments().isEmpty() ){
				// static references does not provide type arguments,
				// even if the list of type parameters is not empty.
				genericMap = Collections.emptyMap();
			}else {
				genericMap = Mapper.mapping( tem.typeParameters(), t.getTypeArguments() );
			}
		}

		/**
		 * Resolves an identifier using this context.
		 * @param identifier The name of the symbol.
		 * @return A {@link Template} representing the symbol.
		 */
		public Template resolveIdentifier( String identifier ) {
			return tem.resolveIdentifier( identifier );
		}

		/**
		 * Replaces a generic with a value from type arguments if given.
		 * May be another generic or the generic itself.
		 * @param identifier the name of the generic
		 * @return The type of the generic
		 */
		public DType replaceGeneric( String identifier ){
			DType tNode = this.funcGenericMap.get( identifier );
			if( tNode != null ){
				return tNode;
			}
			return this.genericMap.get( identifier );
		}

		private void setFuncGenericMap( List< ? extends Template > generics ){
			funcGenericMap = Mapper.mapping( generics, Template::getName,
					g -> new DType( g, Collections.emptyList(), Collections.emptyList() ) );
		}

		/**
		 * Set the method currently processed.
		 * @param methodSig The signature of the method currently being processed.
		 */
		public void setCurrentMethod( Template.MethodSig methodSig ){
			setFuncGenericMap( methodSig.getTypeParameters() );
			this.methodSig = methodSig;
		}

		/**
		 * Removes all method specific mappings and symbols
		 */
		public void clearCurrentMethod(){
			setFuncGenericMap( Collections.emptyList() );
			this.methodSig = null;
		}

		/**
		 * The return type of the method currently being processed.
		 * @return the return type of the method currently being processed.
		 */
		public DType getReturnType(){
			return methodSig.getReturnType();
		}

		/**
		 * Get {@link Role} instance from identifier
		 * @param identifier The identifier of the role
		 * @return An instance of {@link Role} with the same identifier
		 */
		public Role roleFromName( String identifier ){

			if( identifier.equals( Role.UNBOUND_ROLE ) ){
				return new TemporaryRole();
			}

			for( Role role: roleMap.keySet() ){
				if( role.getName().equals( identifier ) ){
					return role;
				}
			}

			throw new IllegalStateException( "No role with such name: " + identifier );
		}
	}

	/**
	 * Keeps track of the context frame stack, and symbol table
	 */
	private static class Context {
		private final SymbolTable symbolTable;
		private final Deque< ContextFrame > contextFrames;
		private final PackageHandler packageHandler;

		public Context(
				Collection< CompilationUnit > cus,
				Collection< CompilationUnit > headerUnits
		) {
			this.packageHandler = new PackageHandler( cus, headerUnits );
			this.symbolTable = new SymbolTable();
			this.contextFrames = new ArrayDeque<>();
		}

		/**
		 * Maps a type's roles and generics from another class
		 * into the class of the current context.
		 * @param type The source type
		 * @return A new type mapped for the current context
		 */
		private DType mapType( DType type ){
			if( type.getTem().isGeneric() ){
				DType replaceType = currentFrame().replaceGeneric( type.getName() );
				if( replaceType != null ) {
					return new DType( replaceType.getTem(),
							Mapper.map( type.getRoles(), currentFrame().roleMap::get ),
							replaceType.getTypeArguments() );
				}
			}
			return type.copyWithMapping( currentFrame().roleMap );
		}

		public Template getTemplate( String packagePath, Class c ){
			return packageHandler.getTemplate( packagePath, c.name().identifier() );
		}

		public void pushFrame( String packagePath, Class c ){
			pushFrame( getTemplate( packagePath, c ) );
		}

		public void pushFrame( Template template ){
			this.contextFrames.addFirst( new ContextFrame( template ) );
		}

		public void pushFrame( DType t ){
			this.contextFrames.addFirst( new ContextFrame( t ) );
		}

		public void pushRootFrame(){
			// soft resets the stack for resolving arguments.
			this.contextFrames.addFirst( rootFrame() );
		}

		public void popFrame() {
			this.contextFrames.removeFirst();
		}

		public DVariable addSymbol( String identifier, DType type ){
			assert contextFrames.size() == 1;

			if( type.getRoles().isEmpty() ){
				type = new DType( type.getTem(),
						Mapper.map( type.getTem().worldParameters(), (r) -> new TemporaryRole() ),
						type.getTypeArguments() );
			}

			return symbolTable.addSymbol( identifier, type );
		}

		public void enterScope(){
			assert contextFrames.size() == 1; // scope cannot change in a context other than root
			symbolTable.enterScope();
		}

		public void exitScope(){
			assert contextFrames.size() == 1;
			symbolTable.exitScope();
		}

		public ContextFrame currentFrame(){
			return this.contextFrames.getFirst();
		}

		public ContextFrame rootFrame(){
			return this.contextFrames.getLast();
		}

		public Template currentTem() {
			return currentFrame().tem;
		}

		/**
		 * Resolves an identifier for a variable or field
		 * @param identifier the name of the variable or field
		 * @return A node representing the variable or field
		 */
		public DNode resolveIdentifier( String identifier ){
			if( rootFrame() == currentFrame() ){
				// Symbol table can only be accessed if the root frame is the current frame.
				DVariable node = symbolTable.getSymbol( identifier );
				if( node != null ){
					return node;
				}
			}

			DVariable field = currentTem().getField( identifier );
			if( field != null ){
				return new DVariable( field.getName(), mapType( field.getType() ) );
			}

			throw new IllegalStateException();
		}

		public void pushContextOf( DNode node ) {
			if( node instanceof DThis ){
				pushFrame( rootFrame().tem );
			}else {
				pushFrame( node.getType() );
			}
		}

		public DType getTypeFromName( String primitiveName, Role role ){
			return new DType( currentFrame().resolveIdentifier( primitiveName ),
					Collections.singletonList( role ),
					Collections.emptyList() );
		}

		private List< Role > mapWorldToString( List< WorldArgument > worldArguments ){
			return Mapper.map( worldArguments, w -> rootFrame().roleFromName( w.name().identifier() ) );
		}

		/**
		 * Maps a {@link TypeExpression} to {@link DType}
		 * @param n the type to map
		 * @return the mapped type
		 */
		public DType getTypeOfExpression( TypeExpression n ){
			List< DType > typeArgs = new ArrayList<>();
			for( TypeExpression t: n.typeArguments() ){
				typeArgs.add( getTypeOfExpression( t ) );
			}

			DType type = currentFrame().replaceGeneric( n.name().identifier() );
			if( type != null ){
				assert typeArgs.size() == 0;
				return new DType( type.getTem(), mapWorldToString( n.worldArguments() ),
								type.getTypeArguments() );
			}

			return new DType( currentFrame().resolveIdentifier( n.name().identifier() ),
							mapWorldToString( n.worldArguments() ), typeArgs );
		}
	}

	// - - - - - - - - - UTILITY - - - - - - - - - - -

	private < R extends Node > List< DNode > visitAndCollect( List< R > n ) {
		return n.stream().map( e -> e.accept( this ) ).collect( Collectors.toList() );
	}
}
