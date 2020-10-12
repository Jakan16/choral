package org.choral.compiler.dependencygraph;

import org.choral.ast.CompilationUnit;
import org.choral.ast.ImportDeclaration;
import org.choral.ast.Name;
import org.choral.ast.Node;
import org.choral.ast.body.Class;
import org.choral.ast.body.Enum;
import org.choral.ast.body.*;
import org.choral.ast.expression.*;
import org.choral.ast.statement.*;
import org.choral.ast.type.FormalTypeParameter;
import org.choral.ast.type.FormalWorldParameter;
import org.choral.ast.type.TypeExpression;
import org.choral.ast.type.WorldArgument;
import org.choral.ast.visitors.ChoralVisitorInterface;
import org.choral.compiler.dependencygraph.dnodes.*;
import org.choral.compiler.dependencygraph.role.Role;
import org.choral.compiler.dependencygraph.symboltable.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Creates a graph for the dependencies required to complete an expression.
 */
public class DependencyGraph implements ChoralVisitorInterface<List< DNode >> {

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
	 */
	public static void walk(
			Collection< CompilationUnit > cus,
			Collection< CompilationUnit > headerUnits
	){
		List< DNode > roots = new ArrayList<>();
		// walk all source nodes
		for( CompilationUnit cu: cus ) {
			roots.addAll( new DependencyGraph( cus, headerUnits ).visit( cu ) );
		}

		//GraphSolver.solve( roots );

		for( DNode dNode: roots ){
			print(dNode, 0);
			System.out.println("---------------");
		}
	}

	private static void print( DNode node, int indent ){
		for( int i = 0; i < indent; i++ ) {
			System.out.print("    ");
		}

		System.out.println(node.toString());
		for( DNode dNode: node.getDependencies() ){
			print( dNode, indent+1 );
		}
	}

	@Override
	public List< DNode > visit( CompilationUnit n ) {
		List< DNode > nodes = new LinkedList<>();
		for( Class c : n.classes() ) {
			this.context.pushFrame( n.packageDeclaration(), c );
			nodes.addAll( visit( c ) );
			this.context.popFrame();
		}
		return nodes;
	}

	@Override
	public List< DNode > visit( ImportDeclaration n ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List< DNode > visit( Class n ) {
		List< DNode > nodes = new ArrayList<>();
		ContextFrame frame = context.currentFrame();
		for( ClassMethodDefinition definition: n.methods() ){
			// the context frame stack may change during a visit to a method,
			// but should return to the original state when the visit finishes.
			assert frame == context.currentFrame();
			frame.setCurrentMethod( context.currentTem().getMethodSig( definition ) );
			nodes.addAll( definition.accept( this ) );
		}
		frame.clearCurrentMethod();

		for( ConstructorDefinition definition: n.constructors() ){
			nodes.addAll( definition.accept( this ) );
		}

		return nodes;
	}

	@Override
	public List< DNode > visit( Enum n ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List< DNode > visit( Interface n ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List< DNode > visit( Statement n ) {
		return n.accept( this );
	}

	@Override
	public List< DNode > visit( BlockStatement n ) {
		context.enterScope();
		List< DNode > nodes = new ArrayList<>( n.enclosedStatement().accept( this ) );
		context.exitScope();
		nodes.addAll( n.continuation().accept( this ) );
		return nodes;
	}

	@Override
	public List< DNode > visit( SelectStatement n ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List< DNode > visit( ScopedExpression n ) {
		List< DNode > dependencies = new ArrayList<>( safeVisit( n.scope() ) );
		assert dependencies.size() == 1;
		context.pushContextOf( dependencies.get( 0 ) ); // pushes context of the scope
		List< DNode > scopedExp = safeVisit( n.scopedExpression() );
		dependencies.addAll( scopedExp );
		context.popFrame();
		if( dependencies.get( 0 ) instanceof DThis ){
			// This and Super has no role information,
			// and the graph can be simplified by omitting these.
			return scopedExp;
		}
		return Collections.singletonList( new DExpression( dependencies, "ScopedExpression" ) );
	}

	@Override
	public List< DNode > visit( ExpressionStatement n ) {
		List< DNode > nodes = new ArrayList<>( n.expression().accept( this ) );
		nodes.addAll( n.continuation().accept( this ) );
		return nodes;
	}

	@Override
	public List< DNode > visit( IfStatement n ) {
		List< DNode > nodes = new ArrayList<>( n.condition().accept( this ) );
		context.enterScope();
		nodes.addAll( n.ifBranch().accept( this ) );
		context.exitScope();
		context.enterScope();
		nodes.addAll( n.elseBranch().accept( this ) );
		context.exitScope();
		nodes.addAll( n.continuation().accept( this ) );
		return nodes;
	}

	@Override
	public List< DNode > visit( SwitchStatement n ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List< DNode > visit( TryCatchStatement n ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List< DNode > visit( NilStatement n ) {
		return Collections.emptyList();
	}

	@Override
	public List< DNode > visit( ReturnStatement n ) {
		List< DNode > dependencies = new ArrayList<>( n.returnExpression().accept( this ) );
		List< DNode > nodes = new ArrayList<>();
		// create explicit return node, as the return may be required to be at specific roles
		nodes.add( new DReturn( dependencies, context.currentFrame().getReturnType() ) );
		nodes.addAll( n.continuation().accept( this ) );
		return nodes;
	}

	@Override
	public List< DNode > visit( Expression n ) {
		return n.accept( this );
	}

	@Override
	public List< DNode > visit( AssignExpression n ) {
		List< DNode > dependencies = new ArrayList<>();
		dependencies.addAll( safeVisit( n.value() ) );
		dependencies.addAll( safeVisit( n.target() ) );
		DExpression dExpression = new DExpression( dependencies, "AssignExpression" );
		return Collections.singletonList( dExpression );
	}

	@Override
	public List< DNode > visit( BinaryExpression n ) {
		List< DNode > dependencies = new ArrayList<>();
		dependencies.addAll( safeVisit( n.left() ) );
		dependencies.addAll( safeVisit( n.right() ) );
		return Collections.singletonList( new DBinaryExpression( dependencies, n.operator() ) );
	}

	@Override
	public List< DNode > visit( EnumCaseInstantiationExpression n ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List< DNode > visit( EnclosedExpression n ) {
		return Collections.singletonList( new DExpression( safeVisit( n.nestedExpression() ),
				"EnclosedExpression" ) );
	}

	@Override
	public List< DNode > visit( FieldAccessExpression n ) {
		return Collections.singletonList( context.resolveIdentifier( n.name().identifier() ) );
	}

	@Override
	public List< DNode > visit( StaticAccessExpression n ) {
		return Collections.singletonList(
				new DStaticAccess( context.getTypeOfExpression( n.typeExpression() ) ) );
	}

	@Override
	public List< DNode > visit( MethodCallExpression n ) {
		Template.MethodSig sig = context.currentTem().getMethodSig( n );
		DType mrt = context.mapType( sig.getReturnType() );

		// return may have the type of a generic given by the function,
		// if this is the case the instance of the generic must be used.
		for( int i = 0; i < sig.getTypeParameters().size(); i++ ) {
			if( sig.getReturnType().getTem() == sig.getTypeParameters().get( i ) ){
				DType rrt = context.getTypeOfExpression( n.typeArguments().get( i ) );
				mrt = new DType( rrt.getTem(), mrt.getRoles(), rrt.getTypeArguments() );
				break;
			}
		}

		List< DNode > arguments = new ArrayList<>();

		for( Expression exp: n.arguments() ){
			// Each argument must be resolved in the context of the currently processed class
			// and not the class of the method.
			this.context.pushRootFrame();
			arguments.addAll( exp.accept( this ) );
			this.context.popFrame();
		}

		DMethodCall node = new DMethodCall( sig.getName(), arguments, mrt,
				Mapper.map( sig.getParameters(), context::mapType ) );
		return Collections.singletonList( node );
	}

	@Override
	public List< DNode > visit( ClassInstantiationExpression n ) {
		List< DNode > dependencies = new ArrayList<>();

		// Each argument must be resolved in the context of the currently processed class
		// and not the class of the constructor.
		for( Expression exp: n.arguments() ){
			this.context.pushRootFrame();
			dependencies.addAll( exp.accept( this ) );
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
		return Collections.singletonList(
				new DClassInstantiation( dependencies, type.getName(), type, parameters ) );
	}

	@Override
	public List< DNode > visit( Name n ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List< DNode > visit( NotExpression n ) {
		return Collections.singletonList( new DExpression( n.expression().accept( this ),
				"NotExpression" ) );
	}

	@Override
	public List< DNode > visit( ThisExpression n ) {
		return Collections.singletonList( new DThis() );
	}

	@Override
	public List< DNode > visit( SuperExpression n ) {
		return Collections.singletonList( new DThis() );
	}

	@Override
	public List< DNode > visit( NullExpression n ) {
		Template nullTemplate = context.currentFrame().resolveIdentifier( "null" );
		DType type = new DType( nullTemplate, context.mapWorldToString( n.worlds() ), Collections.emptyList() );
		return Collections.singletonList( new DLiteral( type ) );
	}

	@Override
	public List< DNode > visit( VariableDeclarationStatement n ) {
		List< DNode > nodes = visitAndCollect( n.variables() );
		nodes.addAll( n.continuation().accept( this ) );
		return nodes;
	}

	@Override
	public List< DNode > visit( BlankExpression n ) {
		return Collections.emptyList();
	}

	@Override
	public List< DNode > visit( LiteralExpression.BooleanLiteralExpression n ) {
		DType type = context.getTypeFromName( "bool", context.rootFrame().roleFromName(n.world().name().identifier() ) );
		return Collections.singletonList( new DLiteral( type ) );
	}

	@Override
	public List< DNode > visit( LiteralExpression.DoubleLiteralExpression n ) {
		DType type = context.getTypeFromName( "double", context.rootFrame().roleFromName(n.world().name().identifier() ) );
		return Collections.singletonList( new DLiteral( type ) );
	}

	@Override
	public List< DNode > visit( LiteralExpression.IntegerLiteralExpression n ) {
		DType type = context.getTypeFromName( "int", context.rootFrame().roleFromName(n.world().name().identifier() ) );
		return Collections.singletonList( new DLiteral( type ) );
	}

	@Override
	public List< DNode > visit( LiteralExpression.StringLiteralExpression n ) {
		DType type = context.getTypeFromName( "String", context.rootFrame().roleFromName(n.world().name().identifier() ) );
		return Collections.singletonList( new DLiteral( type ) );
	}

	@Override
	public List< DNode > visit( SwitchArgument< ? > n ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List< DNode > visit( CaseSignature n ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List< DNode > visit( Field n ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List< DNode > visit( FormalMethodParameter n ) {
		context.addSymbol( n.name().identifier(), context.getTypeOfExpression( n.type() ) );
		return Collections.emptyList();
	}

	@Override
	public List< DNode > visit( ClassMethodDefinition n ) {
		context.enterScope();
		// The signature must be visited to add its symbols to the symbol table,
		// the result is not needed for the graph.
		n.signature().accept( this );
		List< DNode > nodes = n.body().map( this::visit ).orElse( Collections.emptyList() );
		context.exitScope();
		return nodes;
	}

	@Override
	public List< DNode > visit( InterfaceMethodDefinition n ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List< DNode > visit( MethodSignature n ) {
		n.parameters().forEach( p -> p.accept( this ) );
		return Collections.emptyList();
	}

	@Override
	public List< DNode > visit( ConstructorDefinition n ) {
		context.enterScope();
		// The signature must be visited to add its symbols to the symbol table,
		// the result is not needed for the graph.
		n.signature().accept( this );
		List< DNode > nodes = n.body().accept( this );
		context.exitScope();
		return nodes;
	}

	@Override
	public List< DNode > visit( ConstructorSignature n ) {
		n.parameters().forEach( p -> p.accept( this ) );
		return Collections.emptyList();
	}

	@Override
	public List< DNode > visit( VariableDeclaration n ) {
		// Not needed for the final graph,
		// but used for resolving identifiers.
		context.addSymbol( n.name().identifier(), context.getTypeOfExpression( n.type() ) );
		return Collections.emptyList();
	}

	@Override
	public List< DNode > visit( TypeExpression n ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List< DNode > visit( WorldArgument n ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List< DNode > visit( FormalTypeParameter n ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List< DNode > visit( FormalWorldParameter n ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List< DNode > visit( Annotation n ) {
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
			for( Role role: roleMap.keySet() ){
				if( role.getName().equals( identifier ) ){
					return role;
				}
			}

			throw new IllegalStateException( "No role with such name" );
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

		private Template getTemplate( String packagePath, Class c ){
			return packageHandler.getTemplate( packagePath, c.name().identifier() );
		}

		public void pushFrame( String packagePath, Class c ){
			this.contextFrames.addFirst( new ContextFrame( getTemplate( packagePath, c ) ) );
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

		public void addSymbol( String identifier, DType type ){
			assert contextFrames.size() == 1;
			symbolTable.addSymbol( identifier, type );
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
				pushRootFrame();
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
		return n.stream().map( e -> e.accept( this ) )
				.flatMap( Collection::stream ).collect( Collectors.toList() );
	}

	private < R extends Node > List< DNode > safeVisit( R n ) {
		return n == null ? Collections.emptyList() : n.accept( this );
	}
}
