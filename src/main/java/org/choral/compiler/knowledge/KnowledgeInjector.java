package org.choral.compiler.knowledge;

import org.choral.ast.*;
import org.choral.ast.expression.*;
import org.choral.ast.statement.ExpressionStatement;
import org.choral.ast.statement.IfStatement;
import org.choral.ast.statement.Statement;
import org.choral.ast.type.TypeExpression;
import org.choral.ast.type.WorldArgument;
import org.choral.ast.visitors.ChoralVisitor;
import org.choral.compiler.Typer;
import org.choral.compiler.merge.MergeException;
import org.choral.compiler.merge.StatementsMerger;
import org.choral.compiler.soloist.StatementsProjector;
import org.choral.types.GroundDataType;
import org.choral.types.World;

import java.util.*;
import java.util.stream.Collectors;

public class KnowledgeInjector extends ChoralVisitor {

	private final RoleScopes roleScopes = new RoleScopes();
	private final boolean addImport;
	private final static String SELECT = "select";
	private final static String ENUMIMPORT = "choral.choice.Choice";
	private final static String ENUM = "Choice";
	private final static String L = "L";
	private final static String R = "R";
	private final static String AUTOPOSTEXT = "Auto generated";

	private KnowledgeInjector(boolean addImport) {
		this.addImport = addImport;
	}

	public static Collection<CompilationUnit> inject( Collection< CompilationUnit > compilationUnits, Collection< CompilationUnit > headerUnits ){
		Collection< CompilationUnit > cus = compilationUnits;
		for( int i = 0; i < 50; i++ ) {
			KnowledgeInjector injector = new KnowledgeInjector( i == 0 );
			cus = cus.stream()
					.map( cu -> (CompilationUnit) injector.visit( cu ) )
					.collect(Collectors.toList());
			Typer.annotate( cus, headerUnits );
			if( !injector.roleScopes.isModified() ){
				break;
			}
		}
		return cus;
	}

	@Override
	public Node visit( CompilationUnit n ) {
		CompilationUnit copy = (CompilationUnit) super.visit( n );
		if( addImport ){
			copy.imports().add( new ImportDeclaration( ENUMIMPORT, generatedPosition() ) );
		}
		return copy;
	}

	@Override
	public Node visit( IfStatement n ) {
		String choosingRole = getRole( n.condition() );

		//Expression conditionCopy = (Expression) n.condition().accept( this );
		Expression conditionCopy = (Expression) this.visit( n.condition() );

		roleScopes.scope( choosingRole );
		Statement ifBranch = (Statement) n.ifBranch().accept( this );
		RoleScope ifRoles = roleScopes.getRoles();
		boolean childScopesModified = roleScopes.isModified();
		roleScopes.exitScope();

		roleScopes.scope( choosingRole );
		Statement elseBranch = (Statement) n.elseBranch().accept( this );
		RoleScope elseRoles = roleScopes.getRoles();
		childScopesModified |= roleScopes.isModified();
		roleScopes.exitScope();

		if( !childScopesModified ) {
			for( String role : ifRoles.roles() ) {
				if( !elseRoles.contains( role ) || !mergeable( role, n.ifBranch(), n.elseBranch() ) ) {
					String channel = getChannel( role, choosingRole );
					ifBranch   = createSelectStatement( channel, choosingRole, L, ifBranch );
					elseBranch = createSelectStatement( channel, choosingRole, R, elseBranch );
					roleScopes.mark();
				}
			}

			for( String role : elseRoles.roles() ) {
				if( !ifRoles.contains( role ) ) {
					String channel = getChannel( role, choosingRole );
					ifBranch   = createSelectStatement( channel, choosingRole, L, ifBranch );
					elseBranch = createSelectStatement( channel, choosingRole, R, elseBranch );
					roleScopes.mark();
				}
			}
		}


		IfStatement ifStatement = new IfStatement(
				conditionCopy,
				ifBranch,
				elseBranch,
				(Statement) n.continuation().accept( this ),
				n.position()
		);

		return ifStatement;
	}

	@Override
	public Node visit( FieldAccessExpression n ) {
		collectRoles( n );
		return super.visit( n );
	}

	@Override
	public Node visit( Expression n ) {
		collectRoles( n );
		return super.visit( n );
	}

	@Override
	public Node visit( TypeExpression n ) {
		if( n.typeAnnotation().isPresent() ) {
			try {
				roleScopes.addRoles( ( (GroundDataType) n.typeAnnotation().get() ).worldArguments()
						.stream().map( World::identifier )
						.collect( Collectors.toList() ) );
			}catch( ClassCastException ignored ){}
		}
		return super.visit( n );
	}

	@Override
	public Node visit( MethodCallExpression n ) {
		roleScopes.addRoles( n.methodAnnotation().get().higherCallable().declarationContext()
				.worldArguments().stream().map( World::identifier )
				.collect( Collectors.toList()) );
		if( !n.methodAnnotation().get().returnType().isVoid() ){
			roleScopes.addRoles( ((GroundDataType) n.methodAnnotation().get().returnType())
					.worldArguments().stream().map( World::identifier )
					.collect( Collectors.toList()) );
		}
		return super.visit( n );
	}

	private boolean mergeable(String role, Statement statement1, Statement statement2){
		try {
			WorldArgument pRole = new WorldArgument( new Name( role ) );
			StatementsMerger.merge( Arrays.asList(
					StatementsProjector.visit( pRole, statement1 ),
					StatementsProjector.visit( pRole, statement2 ) )
			);
			return true;
		} catch( MergeException e ) {
			return false;
		}
	}

	private void collectRoles(Expression n){
		roleScopes.addRoles( ((GroundDataType) n.typeAnnotation().get()).worldArguments()
				.stream().map( World::identifier )
				.collect( Collectors.toList()) );
	}

	private String getRole( Expression e ){
		if( e instanceof MethodCallExpression ){
			return ((GroundDataType) ((MethodCallExpression) e).methodAnnotation().get().returnType()).worldArguments().get( 0 ).identifier();
		}
		return ((GroundDataType) e.typeAnnotation().get()).worldArguments().get( 0 ).identifier();

	}

	private String getChannel( String role1, String role2 ){
		int diff = role1.compareTo( role2 );
		assert diff != 0;
		if( diff < 0 ){
			return "ch_" + role1 + "_" + role2;
		}else {
			return "ch_" + role2 + "_" + role1;
		}
	}

	private static ExpressionStatement createSelectStatement( String channel, String sender, String Label, Statement continuation ){

		return new ExpressionStatement(
				new ScopedExpression(
						new FieldAccessExpression( new Name( channel ), generatedPosition() ),
						new MethodCallExpression(
								new Name( SELECT ),
								Collections.singletonList(
										new ScopedExpression(
												new StaticAccessExpression(
														new TypeExpression(
																new Name( ENUM ),
																Collections.singletonList( new WorldArgument( new Name( sender ) ) ),
																Collections.emptyList(),
																generatedPosition()
														)
												),
												new FieldAccessExpression( new Name( Label ), generatedPosition() ),
												generatedPosition()
										)
								),
								Collections.singletonList( new TypeExpression( new Name( ENUM ), Collections.emptyList(), Collections.emptyList() ) ),
								generatedPosition()
						),
						generatedPosition()
				),
				continuation,
				generatedPosition()
		);
	}

	private static Position generatedPosition(){
		return new Position( AUTOPOSTEXT, 0, 0 );
	}

	private static class RoleScopes{

		final Deque< RoleScope > scopes;

		public RoleScopes( ) {
			scopes = new ArrayDeque<>();
			scopes.add( new RoleScope() );
		}

		void addRole( String r ){
			scopes.getFirst().addRole( r );
		}

		void addRoles( Collection< String > rs ){
			scopes.getFirst().addRoles( rs );
		}

		void scope( String owner ){
			scopes.addFirst( new RoleScope( owner ) );
		}

		void exitScope(){
			RoleScope prevHead = scopes.removeFirst();
			scopes.getFirst().addRoles( prevHead.roles() );
			if( prevHead.isModified() ) {
				scopes.getFirst().mark();
			}
		}

		RoleScope getRoles(){
			return scopes.getFirst();
		}

		void mark(){
			scopes.getFirst().mark();
		}

		boolean isModified() {
			return scopes.getFirst().isModified();
		}
	}

	private static class RoleScope{
		Set< String > roles;
		boolean modified;
		String owner;

		RoleScope(String owner) {
			this.roles = new HashSet<>();
			this.modified = false;
			this.owner = owner;
		}

		RoleScope() {
			this.roles = new HashSet<>();
			this.modified = false;
			this.owner = "";
		}

		void mark(){
			modified = true;
		}

		boolean isModified() {
			return modified;
		}

		Collection< String > roles(){
			return roles;
		}

		void addRole( String r ){
			if( !r.equals( owner ) ){
				roles.add( r );
			}
		}

		void addRoles( Collection< String > rs ){
			roles.addAll( rs );
			roles.remove( owner );
		}

		boolean contains( String role ){
			return roles.contains( role );
		}
	}
}
