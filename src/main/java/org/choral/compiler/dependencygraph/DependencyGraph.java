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
		for( ClassMethodDefinition definition: n.methods() ){
			context.currentFrame().setFuncGenericMap(
					context.currentTem().getMethodSig( definition ).getTypeParameters() );
			nodes.addAll( definition.accept( this ) );
		}
		context.currentFrame().setFuncGenericMap( Collections.emptyList() );

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
		dependencies.addAll( safeVisit( n.scopedExpression() ) );
		context.popFrame();
		return Collections.singletonList( new ExpressionDNode( dependencies, "ScopedExpression" ) );
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
		List< DNode > nodes = new ArrayList<>( n.returnExpression().accept( this ) );
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
		ExpressionDNode expressionDNode = new ExpressionDNode( dependencies, "AssignExpression" );
		return Collections.singletonList( expressionDNode );
	}

	@Override
	public List< DNode > visit( BinaryExpression n ) {
		List< DNode > dependencies = new ArrayList<>();
		dependencies.addAll( safeVisit( n.left() ) );
		dependencies.addAll( safeVisit( n.right() ) );
		ExpressionDNode expressionDNode = new ExpressionDNode( dependencies, "BinaryExpression" );
		return Collections.singletonList( expressionDNode );
	}

	@Override
	public List< DNode > visit( EnumCaseInstantiationExpression n ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List< DNode > visit( EnclosedExpression n ) {
		return Collections.singletonList( new ExpressionDNode( safeVisit( n.nestedExpression() ),
				"EnclosedExpression" ) );
	}

	@Override
	public List< DNode > visit( FieldAccessExpression n ) {
		return Collections.singletonList( context.resolveIdentifier( n.name().identifier() ) );
	}

	@Override
	public List< DNode > visit( StaticAccessExpression n ) {
		return n.typeExpression().accept( this );
	}

	@Override
	public List< DNode > visit( MethodCallExpression n ) {
		Template.MethodSig sig = context.currentTem().getMethodSig( n );
		TypeDNode mrt = context.mapType( sig.getReturnType() );

		for( int i = 0; i < sig.getTypeParameters().size(); i++ ) {
			if( sig.getReturnType().getTem() == sig.getTypeParameters().get( i ) ){
				TypeDNode rrt = n.typeArguments().get( i ).accept( this ).get( 0 ).getType();
				mrt = new TypeDNode( rrt.getTem(), mrt.getRoles(), rrt.getTypeArguments() );
				break;
			}
		}

		MethodCallDNode node = new MethodCallDNode( visitAndCollect( n.arguments() ),
				mrt,
				Mapper.map( sig.getParameters(), context::mapType ) );
		node.setRole( mrt.getRoles() );
		return Collections.singletonList( node );
	}

	@Override
	public List< DNode > visit( ClassInstantiationExpression n ) {
		List< DNode > dependencies = visitAndCollect( n.arguments() );
		TypeDNode type = (TypeDNode) n.typeExpression().accept( this ).get( 0 );
		return Collections.singletonList(
				new ClassInstantiationDNode( dependencies, type.getName(), type ) );
	}

	@Override
	public List< DNode > visit( Name n ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List< DNode > visit( NotExpression n ) {
		return Collections.singletonList( new ExpressionDNode( n.expression().accept( this ), "NotExpression" ) );
	}

	@Override
	public List< DNode > visit( ThisExpression n ) {
		return Collections.singletonList( new ThisDNode() );
	}

	@Override
	public List< DNode > visit( SuperExpression n ) {
		return Collections.singletonList( new ThisDNode() );
	}

	@Override
	public List< DNode > visit( NullExpression n ) {
		LiteralDNode node = new LiteralDNode( "NullExpression" );
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
		LiteralDNode node = new LiteralDNode( "BooleanLiteralExpression" );
		node.setRole( Collections.singletonList( n.world().name().identifier() ) );
		return Collections.singletonList( node );
	}

	@Override
	public List< DNode > visit( LiteralExpression.DoubleLiteralExpression n ) {
		LiteralDNode node = new LiteralDNode( "DoubleLiteralExpression" );
		node.setRole( Collections.singletonList( n.world().name().identifier() ) );
		return Collections.singletonList( node );
	}

	@Override
	public List< DNode > visit( LiteralExpression.IntegerLiteralExpression n ) {
		LiteralDNode node = new LiteralDNode( "IntegerLiteralExpression" );
		node.setSource( n );
		node.setRole( Collections.singletonList( n.world().name().identifier() ) );
		return Collections.singletonList( node );
	}

	@Override
	public List< DNode > visit( LiteralExpression.StringLiteralExpression n ) {
		LiteralDNode node = new LiteralDNode( "StringLiteralExpression" );
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
		context.addSymbol( n.name().identifier(), (TypeDNode) n.type().accept( this ).get( 0 ) );
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
		context.addSymbol( n.name().identifier(), (TypeDNode) n.type().accept( this ).get( 0 ) );
		return Collections.emptyList();
	}

	@Override
	public List< DNode > visit( TypeExpression n ) {
		List< TypeDNode > typeArgs = new ArrayList<>();
		for( TypeExpression t: n.typeArguments() ){
			typeArgs.add( (TypeDNode) t.accept( this ).get( 0 ) );
		}

		TypeDNode type = context.currentFrame().replaceGeneric( n.name().identifier() );
		if( type != null ){
			assert typeArgs.size() == 0;
			return Collections.singletonList(
					new TypeDNode( type.getTem(), mapWorldToString( n.worldArguments() ),
							type.getTypeArguments() ) );
		}

		return Collections.singletonList(
				new TypeDNode( context.currentFrame().resolveIdentifier( n.name().identifier() ),
				mapWorldToString( n.worldArguments() ), typeArgs ) );
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

	public List< String > mapWorldToString( List< WorldArgument > worldArguments ){
		return Mapper.map( worldArguments, w -> w.name().identifier() );
	}

	private static class ContextFrame {
		private final Template tem;
		private final Map< String, String > roleMap;
		private final Map< String, TypeDNode > genericMap;
		private Map< String, TypeDNode > funcGenericMap = Collections.emptyMap();

		public ContextFrame( Template tem ) {
			assert tem != null;
			this.tem = tem;

			roleMap = Mapper.idMap(
					tem.worldParameters(),
					w -> w.toWorldArgument().name().identifier() );
			genericMap = Collections.emptyMap();
		}

		public ContextFrame( TypeDNode t ) {
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

		public TypeDNode replaceGeneric( String identifier ){
			TypeDNode tNode = this.funcGenericMap.get( identifier );
			if( tNode != null ){
				return tNode;
			}
			return this.genericMap.get( identifier );
		}

		public void setFuncGenericMap( List< ? extends Template > generics ){
			funcGenericMap = Mapper.mapping( generics, Template::getName,
					g -> new TypeDNode( g, Collections.emptyList(), Collections.emptyList() ) );
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

		private TypeDNode mapType( TypeDNode type ){
			if( type.getTem().isGeneric() ){
				TypeDNode replaceType = currentFrame().replaceGeneric( type.getName() );
				if( replaceType != null ) {
					return new TypeDNode( replaceType.getTem(),
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

		public void pushFrame( TypeDNode t ){
			this.contextFrames.addFirst( new ContextFrame( t ) );
		}

		public void pushCurrentFrame(){
			this.contextFrames.addFirst( this.contextFrames.getFirst() );
		}

		public void popFrame() {
			this.contextFrames.removeFirst();
		}

		public void addSymbol( String identifier, TypeDNode type ){
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

		public Template currentTem() {
			return currentFrame().tem;
		}

		public DNode resolveIdentifier(String identifier){
			//if( this.contextFrames.size() == 1 ){
				VariableDNode node = symbolTable.getSymbol( identifier );
				if( node != null ){
					return node;
				}
			//}

			VariableDNode field = currentTem().getField( identifier );
			if( field != null ){
				return new VariableDNode( field.getName(), mapType( field.getType() ) );
			}

			throw new IllegalStateException();
		}

		public void pushContextOf( DNode node ) {
			if( node instanceof ThisDNode ){
				pushCurrentFrame();
			}else {
				TypeDNode t = node.getType();
				pushFrame( t );
			}
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
