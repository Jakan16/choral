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
import org.choral.compiler.dependencygraph.symboltable.*;

import java.util.*;
import java.util.stream.Collectors;

public class DependencyGraph implements ChoralVisitorInterface<List< DNode >> {

	private final Context context;

	public DependencyGraph( Collection< CompilationUnit > cus,
							Collection< CompilationUnit > headerUnits
	) {
		this.context = new Context( cus, headerUnits );
	}

	public static void walk(
			Collection< CompilationUnit > cus,
			Collection< CompilationUnit > headerUnits
	){
		List< DNode > roots = new ArrayList<>();
		for( CompilationUnit cu: cus ) {
			roots.addAll( new DependencyGraph( cus, headerUnits ).visit( cu ) );
		}

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
		context.pushContextOf( dependencies.get( 0 ) );
		List< DNode > scopedExp = safeVisit( n.scopedExpression() );
		dependencies.addAll( scopedExp );
		context.popFrame();
		if( dependencies.get( 0 ) instanceof DThis ){
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
		DExpression dExpression = new DExpression( dependencies, "BinaryExpression" );
		return Collections.singletonList( dExpression );
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
		return Collections.singletonList( new DStaticAccess( context.getTypeOfExpression( n.typeExpression() ) ) );
	}

	@Override
	public List< DNode > visit( MethodCallExpression n ) {
		Template.MethodSig sig = context.currentTem().getMethodSig( n );
		DType mrt = context.mapType( sig.getReturnType() );

		for( int i = 0; i < sig.getTypeParameters().size(); i++ ) {
			if( sig.getReturnType().getTem() == sig.getTypeParameters().get( i ) ){
				DType rrt = context.getTypeOfExpression( n.typeArguments().get( i ) );
				mrt = new DType( rrt.getTem(), mrt.getRoles(), rrt.getTypeArguments() );
				break;
			}
		}

		List< DNode > arguments = new ArrayList<>();

		for( Expression exp: n.arguments() ){
			this.context.pushRootFrame();
			arguments.addAll( exp.accept( this ) );
			this.context.popFrame();
		}

		DMethodCall node = new DMethodCall( arguments, mrt,
				Mapper.map( sig.getParameters(), context::mapType ) );
		node.setRole( mrt.getRoles() );
		return Collections.singletonList( node );
	}

	@Override
	public List< DNode > visit( ClassInstantiationExpression n ) {
		List< DNode > dependencies = new ArrayList<>();

		for( Expression exp: n.arguments() ){
			this.context.pushRootFrame();
			dependencies.addAll( exp.accept( this ) );
			this.context.popFrame();
		}

		DType type = context.getTypeOfExpression( n.typeExpression() );
		Template.MethodSig sig = type.getTem().getConstructorSig( n.arguments().size() );
		Map< String, String > roleMap = Mapper.mapping(
				type.getTem().worldParameters(), type.getRoles(),
				p -> p.toWorldArgument().name().identifier(), Mapper.id() );
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
		return Collections.singletonList( new DExpression( n.expression().accept( this ), "NotExpression" ) );
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
		DLiteral node = new DLiteral( "NullExpression" );
		node.setRole( mapWorldToString( n.worlds() ) );
		return Collections.singletonList( node );
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
		DLiteral node = new DLiteral( "BooleanLiteralExpression" );
		node.setRole( Collections.singletonList( n.world().name().identifier() ) );
		return Collections.singletonList( node );
	}

	@Override
	public List< DNode > visit( LiteralExpression.DoubleLiteralExpression n ) {
		DLiteral node = new DLiteral( "DoubleLiteralExpression" );
		node.setRole( Collections.singletonList( n.world().name().identifier() ) );
		return Collections.singletonList( node );
	}

	@Override
	public List< DNode > visit( LiteralExpression.IntegerLiteralExpression n ) {
		DLiteral node = new DLiteral( "IntegerLiteralExpression" );
		node.setSource( n );
		node.setRole( Collections.singletonList( n.world().name().identifier() ) );
		return Collections.singletonList( node );
	}

	@Override
	public List< DNode > visit( LiteralExpression.StringLiteralExpression n ) {
		DLiteral node = new DLiteral( "StringLiteralExpression" );
		node.setRole( Collections.singletonList( n.world().name().identifier() ) );
		return Collections.singletonList( node );
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

	public static List< String > mapWorldToString( List< WorldArgument > worldArguments ){
		return Mapper.map( worldArguments, w -> w.name().identifier() );
	}

	private static class ContextFrame {
		private final Template tem;
		private final Map< String, String > roleMap;
		private final Map< String, DType > genericMap;
		private Template.MethodSig methodSig;
		private Map< String, DType > funcGenericMap = Collections.emptyMap();

		public ContextFrame( Template tem ) {
			assert tem != null;
			this.tem = tem;

			roleMap = Mapper.idMap(
					tem.worldParameters(),
					w -> w.toWorldArgument().name().identifier() );
			genericMap = Collections.emptyMap();
		}

		public ContextFrame( DType t ) {
			Template tem = t.getTem();
			assert tem != null;
			this.tem = tem;

			roleMap = Mapper.mapping( tem.worldParameters(), t.getRoles(),
					w -> w.toWorldArgument().name().identifier(), Mapper.id() );
			if( t.getTypeArguments().isEmpty() ){
				genericMap = Collections.emptyMap();
			}else {
				genericMap = Mapper.mapping( tem.typeParameters(), t.getTypeArguments(),
						a -> a.name().identifier(), Mapper.id() );
			}
		}

		public Template resolveIdentifier( String identifier ) {
			return tem.resolveIdentifier( identifier );
		}

		public DType replaceGeneric( String identifier ){
			DType tNode = this.funcGenericMap.get( identifier );
			if( tNode != null ){
				return tNode;
			}
			return this.genericMap.get( identifier );
		}

		public void setFuncGenericMap( List< ? extends Template > generics ){
			funcGenericMap = Mapper.mapping( generics, Template::getName,
					g -> new DType( g, Collections.emptyList(), Collections.emptyList() ) );
		}

		public void setCurrentMethod( Template.MethodSig methodSig ){
			setFuncGenericMap( methodSig.getTypeParameters() );
			this.methodSig = methodSig;
		}

		public void clearCurrentMethod(){
			setFuncGenericMap( Collections.emptyList() );
			this.methodSig = null;
		}

		public DType getReturnType(){
			return methodSig.getReturnType();
		}
	}

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
			assert contextFrames.size() == 1;
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

		public DNode resolveIdentifier(String identifier){
			if( rootFrame() == currentFrame() ){
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
