package org.choral.compiler.dependencygraph;

import org.choral.ast.CompilationUnit;
import org.choral.ast.Name;
import org.choral.ast.Node;
import org.choral.ast.Position;
import org.choral.ast.expression.*;
import org.choral.ast.statement.ReturnStatement;
import org.choral.ast.type.TypeExpression;
import org.choral.ast.type.WorldArgument;
import org.choral.ast.visitors.ChoralVisitor;
import org.choral.compiler.dependencygraph.dnodes.DClassInstantiation;
import org.choral.compiler.dependencygraph.dnodes.DMethodCall;
import org.choral.compiler.dependencygraph.dnodes.DType;
import org.choral.compiler.dependencygraph.role.Role;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ComInjector extends ChoralVisitor {

	public static Collection< CompilationUnit > inject( Collection< CompilationUnit > cus ){
		return cus.stream()
				.map( c -> (CompilationUnit) new ComInjector().visit( c ) )
				.collect( Collectors.toList() );
	}

	@Override
	public Node visit( ReturnStatement n ) {

		Expression exp = safeVisit( n.returnExpression() );

		var targetType = n.getDependencies().getType();
		var sourceType = n.returnExpression().getDependencies().getType();

		return new ReturnStatement(
				insertComIfNecessary( exp, targetType, sourceType ),
				safeVisit( n.continuation() ),
				n.position()
		);
	}

	@Override
	public Node visit( AssignExpression n ) {

		var targetType = n.target().getDependencies().getType();
		var sourceType = n.value().getDependencies().getType();

		return new AssignExpression(
				insertComIfNecessary( safeVisit( n.value() ), targetType, sourceType ),
				safeVisit( n.target() ),
				n.operator(),
				n.position()
		);
	}

	@Override
	public Node visit( BinaryExpression n ) {

		var targetType = n.getDependencies().getType();
		var leftSourceType = n.left().getDependencies().getType();
		var rightSourceType = n.right().getDependencies().getType();

		return new BinaryExpression(
				insertComIfNecessary( safeVisit( n.left() ), targetType, leftSourceType ),
				insertComIfNecessary( safeVisit( n.right() ), targetType, rightSourceType ),
				n.operator(),
				n.position()
		);
	}

	@Override
	public Node visit( ClassInstantiationExpression n ) {

		List< DType > paramTypes =  ((DClassInstantiation) n.getDependencies()).getParameters();
		List< Expression > mappedArgs = Mapper.map( n.arguments(), paramTypes,
				(a, p) -> insertComIfNecessary( safeVisit( a ), p, a.getDependencies().getType() ) );

		TypeExpression typeExpression = safeVisit( n.typeExpression() );

		if( !isFixed( typeExpression.worldArguments().get( 0 ).name() ) ){
			typeExpression = new TypeExpression( typeExpression.name(),
					Collections.singletonList( createWorldArg( n ) ),
					typeExpression.typeArguments() );
		}

		return new ClassInstantiationExpression(
				typeExpression,
				mappedArgs,
				visitAndCollect( n.typeArguments() ),
				n.position()
		);
	}

	@Override
	public Node visit( MethodCallExpression n ) {

		List< DType > paramTypes =  ((DMethodCall) n.getDependencies()).getParameters();
		List< Expression > mappedArgs = Mapper.map( n.arguments(), paramTypes,
				(a, p) -> insertComIfNecessary( safeVisit( a ), p, a.getDependencies().getType() ) );

		return new MethodCallExpression(
				safeVisit( n.name() ),
				mappedArgs,
				visitAndCollect( n.typeArguments() ),
				n.position()
		);
	}

	@Override
	public Node visit( EnclosedExpression n ) {

		var targetType = n.getDependencies().getType();
		var sourceType = n.nestedExpression().getDependencies().getType();

		return new EnclosedExpression(
				safeVisit( insertComIfNecessary( n.nestedExpression(), targetType, sourceType ) ),
				n.position()
		);
	}

	@Override
	public Node visit( NotExpression n ) {

		var targetType = n.getDependencies().getType();
		var sourceType = n.expression().getDependencies().getType();

		return new NotExpression(
				safeVisit( insertComIfNecessary( n.expression(), targetType, sourceType ) ),
				n.position()
		);
	}

	@Override
	public Node visit( LiteralExpression.IntegerLiteralExpression n ) {
		if( isFixed( n.world().name() ) ){
			return n;
		}
		return new LiteralExpression.IntegerLiteralExpression( n.content(), createWorldArg( n ) );
	}

	@Override
	public Node visit( LiteralExpression.BooleanLiteralExpression n ) {
		if( isFixed( n.world().name() ) ){
			return n;
		}

		return new LiteralExpression.BooleanLiteralExpression( n.content(), createWorldArg( n ) );
	}

	@Override
	public Node visit( LiteralExpression.DoubleLiteralExpression n ) {
		if( isFixed( n.world().name() ) ){
			return n;
		}
		return new LiteralExpression.DoubleLiteralExpression( n.content(), createWorldArg( n ) );
	}

	@Override
	public Node visit( LiteralExpression.StringLiteralExpression n ) {
		if( isFixed( n.world().name() ) ){
			return n;
		}
		return new LiteralExpression.StringLiteralExpression( n.content(), createWorldArg( n ) );
	}

	private WorldArgument createWorldArg( Node n ){
		return new WorldArgument( new Name( n.getDependencies().getType().getRoles().get( 0 ).getName() ) );
	}

	private boolean isFixed( Name name ){
		return !name.identifier().equals( Role.UNBOUND_ROLE );
	}

	private Expression insertComIfNecessary( Expression exp, DType target, DType source ){
		if( target.getRoles().size() == 1 && source.getRoles().size() == 1 ){
			var targetRole = target.getRoles().get( 0 );
			var sourceRole = source.getRoles().get( 0 );

			if( !Role.commonDisplayName( targetRole, sourceRole ) ) {
				return wrapInCom( exp, source, target );
			}
		}

		return exp;
	}

	private Expression wrapInCom( Expression exp, DType sourceType, DType targetType ){
		var call = new MethodCallExpression( new Name( "com" ),
				Collections.singletonList( exp ),
				Collections.singletonList( sourceType.toTypeExpression() ),
				new Position( "Generated", 0, 0 ) );

		return new ScopedExpression(
				new FieldAccessExpression(
						new Name( getChannel( sourceType.getRoles().get( 0 ).getName(), targetType.getRoles().get( 0 ).getName() ) ) ),
				call);
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

	// - - - - - - - - - UTILITY - - - - - - - - - - -

	@SuppressWarnings( "unchecked cast" )
	private < T extends Node > List< T > visitAndCollect( List< T > n ) {
		return n.stream().map( e -> (T) e.accept( this ) ).collect( Collectors.toList() );
	}

	@SuppressWarnings( "unchecked cast" )
	private < T extends Node > T safeVisit( T n ) {
		return n == null ? null : (T) n.accept( this );
	}
}
