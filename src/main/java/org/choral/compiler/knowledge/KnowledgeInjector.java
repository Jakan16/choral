package org.choral.compiler.knowledge;

import org.choral.ast.*;
import org.choral.ast.expression.*;
import org.choral.ast.statement.ExpressionStatement;
import org.choral.ast.statement.IfStatement;
import org.choral.ast.statement.Statement;
import org.choral.ast.type.TypeExpression;
import org.choral.ast.type.WorldArgument;
import org.choral.ast.visitors.ChoralVisitor;
import org.choral.ast.visitors.PrettyPrinterVisitor;
import org.choral.compiler.merge.MergeException;
import org.choral.compiler.merge.StatementsMerger;
import org.choral.compiler.soloist.StatementsProjector;
import org.choral.types.*;
import org.choral.types.Package;

import java.util.*;
import java.util.stream.Collectors;

public class KnowledgeInjector extends ChoralVisitor {

	private final SeenRoles seenRoles = new SeenRoles();
	private final static String SELECT = "select";
	private final static String ENUMIMPORT = "choral.choice.Choice";
	private final static String ENUM = "Choice";
	private final static String L = "L";
	private final static String R = "R";
	private final static String AUTOPOSTEXT = "Auto generated";

	private KnowledgeInjector() {}

	public static Collection<CompilationUnit> inject( Collection< CompilationUnit > compilationUnits ){
		List<CompilationUnit> l = compilationUnits.stream()
				.map( cu -> (CompilationUnit) new KnowledgeInjector().visit( cu ) )
				.collect(Collectors.toList());
		PrettyPrinterVisitor ppv = new PrettyPrinterVisitor();
		l.forEach( cu -> System.out.println(ppv.visit( cu )) );
		return l;
	}

	@Override
	public Node visit( CompilationUnit n ) {

		/*try {
			System.out.println(((ExpressionStatement)n.classes().get( 0 ).methods().get( 0 ).body().get()).expression().typeAnnotation().get());
			Universe u = new Universe();
			System.out.println(new HigherEnum( u.rootPackage(), EnumSet.noneOf( Modifier.class ), "", new World( u ,"B" ) ));
			System.out.println("kage");
		}catch( Exception e ){

		}*/

		CompilationUnit copy = (CompilationUnit) super.visit( n );
		copy.imports().add( new ImportDeclaration( ENUMIMPORT, generatedPosition() ) );
		return copy;
	}

	@Override
	public Node visit( IfStatement n ) {
		String choosingRole = getRole( n.condition() );

		n.condition().accept( this );

		seenRoles.scope();
		Statement ifBranchOg = (Statement) n.ifBranch().accept( this );
		Set< String > ifRoles = seenRoles.getRoles();
		seenRoles.exitScope();

		seenRoles.scope();
		Statement elseBranchOg = (Statement) n.elseBranch().accept( this );
		Set< String > elseRoles = seenRoles.getRoles();
		seenRoles.exitScope();

		ifRoles.remove( choosingRole );
		elseRoles.remove( choosingRole );

		System.out.println(choosingRole);
		System.out.println(ifRoles);
		System.out.println(elseRoles);

		Statement ifBranch = ifBranchOg;
		Statement elseBranch = elseBranchOg;

		for( String role : ifRoles ){
			if( !elseRoles.contains( role ) ){
				ifBranch = createSelectStatement( getChannel( role, choosingRole ), choosingRole, L, ifBranch );
				elseBranch = createSelectStatement( getChannel( role, choosingRole ), choosingRole, R, elseBranch );
			}else{
				try {
					StatementsMerger.merge( Arrays.asList(
							StatementsProjector.visit( new WorldArgument( new Name( role ) ) , ifBranchOg ),
							StatementsProjector.visit( new WorldArgument( new Name( role ) ) , elseBranchOg ))
					);
				}catch( MergeException e ){
					ifBranch = createSelectStatement( getChannel( role, choosingRole ), choosingRole, L, ifBranch );
					elseBranch = createSelectStatement( getChannel( role, choosingRole ), choosingRole, R, elseBranch );
				}
			}
		}

		for( String role : elseRoles ){
			if( !ifRoles.contains( role ) ){
				ifBranch = createSelectStatement( getChannel( role, choosingRole ), choosingRole, L, ifBranch );
				elseBranch = createSelectStatement( getChannel( role, choosingRole ), choosingRole, R, elseBranch );
			}
		}

		return new IfStatement(
				(Expression) n.condition().accept( this ),
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
	public Node visit( LiteralExpression.BooleanLiteralExpression n ) {
		collectRoles( n );
		return super.visit( n );
	}

	@Override
	public Node visit( MethodCallExpression n ) {
		/*System.out.println("MethodCallExpression " + n.name());
		if( !n.methodAnnotation().get().returnType().isVoid() )
			System.out.println(((GroundDataType) n.methodAnnotation().get().returnType())
					.worldArguments().stream().map( World::identifier )
					.collect( Collectors.toList()));
		 */
		//TODO check the callee roles
		if( !n.methodAnnotation().get().returnType().isVoid() ){
			seenRoles.addRoles( ((GroundDataType) n.methodAnnotation().get().returnType())
					.worldArguments().stream().map( World::identifier )
					.collect( Collectors.toList()) );
		}
		return super.visit( n );
	}

	private void collectRoles(Expression n){
		seenRoles.addRoles( ((GroundDataType) n.typeAnnotation().get()).worldArguments()
				.stream().map( World::identifier )
				.collect( Collectors.toList()) );
	}

	private String getRole( Expression e ){
		return ((GroundDataType) e.typeAnnotation().get()).worldArguments().get(0).identifier();
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

		FieldAccessExpression fae = new FieldAccessExpression( new Name( channel ), generatedPosition() );
		//fae.setTypeAnnotation( new HigherEnum(  ) );

		return new ExpressionStatement(
				new ScopedExpression(
						fae,
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

	private static class SeenRoles{

		final List< Set< String > > roleGroups;

		public SeenRoles( ) {
			this.roleGroups = new LinkedList<>();
			this.roleGroups.add( new HashSet<>() );
		}

		void addRole( String r ){
			roleGroups.get( 0 ).add( r );
		}

		void addRoles( Collection< String > rs ){
			roleGroups.get( 0 ).addAll( rs );
		}

		void scope(){
			roleGroups.add( 0, new HashSet<>() );
		}

		void exitScope(){
			assert roleGroups.size() >= 2;
			Set< String > prevHead = roleGroups.remove( 0 );
			roleGroups.get( 0 ).addAll( prevHead );
		}

		Set< String > getRoles(){
			return roleGroups.get( 0 );
		}

	}
}
