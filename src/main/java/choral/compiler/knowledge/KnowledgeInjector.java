package choral.compiler.knowledge;

import choral.ast.*;
import choral.ast.expression.*;
import choral.ast.statement.ExpressionStatement;
import choral.ast.statement.IfStatement;
import choral.ast.statement.Statement;
import choral.ast.type.TypeExpression;
import choral.ast.type.WorldArgument;
import choral.ast.visitors.ChoralVisitor;
import choral.compiler.Typer;
import choral.compiler.dependencygraph.ComInjector;
import choral.compiler.dependencygraph.Mapper;
import choral.compiler.dependencygraph.role.Role;
import choral.compiler.merge.MergeException;
import choral.compiler.merge.StatementsMerger;
import choral.compiler.soloist.StatementsProjector;
import choral.types.GroundDataType;
import choral.types.World;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
		Expression condition = (Expression) this.visit( n.condition() );

		// collect roles from if branch
		roleScopes.scope();
		Statement ifBranch = (Statement) n.ifBranch().accept( this );
		RoleScope ifRoles = roleScopes.getRoles();
		boolean childScopesModified = roleScopes.isModified();
		roleScopes.exitScope();

		// collect roles from else branch
		roleScopes.scope();
		Statement elseBranch = (Statement) n.elseBranch().accept( this );
		RoleScope elseRoles = roleScopes.getRoles();
		childScopesModified |= roleScopes.isModified();
		roleScopes.exitScope();

		// do nothing if any branch has been modified during the current iteration.
		if( !childScopesModified ) {
			// set of roles that implement different behaviours in each branch.
			Set< String > selectRoles =
					Stream.concat(
						ifRoles.roles().stream().filter( role -> !elseRoles.contains( role ) || !mergeable( role, n.ifBranch(), n.elseBranch() ) ),
						elseRoles.roles().stream().filter( role -> !ifRoles.contains( role ) ) )
					.collect(Collectors.toSet());

			String choosingRole = getRole( n.condition() );
			// Used when the role of the guard is not fixed,
			// it is then coalesced to one of the roles needing the result of the guard.
			if( n.condition() instanceof LiteralExpression.BooleanLiteralExpression ){
				var con = ((LiteralExpression.BooleanLiteralExpression) n.condition());
				if( con.getOriginalExpression() != null ){
					var rootRole = con.getOriginalExpression().getDependencies()
							.getType().getRoles().get( 0 ).getCanonicalRole();
					var conditionPref = rootRole.getPreferredRoles().stream()
							.map( Role::getName ).filter( selectRoles::contains ).findAny();

					final String roleName = conditionPref.orElseGet( () -> selectRoles.iterator().hasNext() ? selectRoles.iterator().next() : rootRole.getPossibleRoles().get( 0 ).getName() );
					choosingRole = roleName;
					// translate String to Role
					var opRole = rootRole.getPossibleRoles().stream()
							.filter( r -> r.getName().equals( roleName ) ).findAny();

					// coalesce the role if any, otherwise rely on defaults.
					opRole.ifPresent( rootRole::coalesce );
					// calculate communications for the new condition
					condition = ComInjector.inject( con.getOriginalExpression() );
					roleScopes.mark();
				}
			}

			// the role calculating the guard, does not have to inform itself
			selectRoles.remove( choosingRole );

			for( String role : selectRoles ) {
				String channel = getChannel( role, choosingRole );
				ifBranch   = createSelectStatement( channel, choosingRole, L, ifBranch );
				elseBranch = createSelectStatement( channel, choosingRole, R, elseBranch );
			}

			if( selectRoles.size() > 0 ){
				roleScopes.mark();
			}
		}

		return new IfStatement(
				condition,
				ifBranch,
				elseBranch,
				(Statement) n.continuation().accept( this ),
				n.position()
		);
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
		if( n.typeAnnotation().isPresent() ) {
			roleScopes.addRoles( ( (GroundDataType) n.typeAnnotation().get() ).worldArguments()
					.stream().map( World::identifier )
					.collect( Collectors.toList() ) );
		}
	}

	private String getRole( Expression e ){
		if( e instanceof MethodCallExpression ){
			return ((GroundDataType) ((MethodCallExpression) e).methodAnnotation().get().returnType()).worldArguments().get( 0 ).identifier();
		}
		if( e instanceof EnclosedExpression ){
			return getRole( ( (EnclosedExpression) e ).nestedExpression() );
		}
		if( e instanceof ClassInstantiationExpression ){
			return ( (ClassInstantiationExpression) e ).typeExpression().worldArguments().get( 0 ).name().identifier();
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

		void scope(){
			scopes.addFirst( new RoleScope() );
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
