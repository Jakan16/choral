package org.choral.compiler.dependencygraph;

import org.choral.compiler.dependencygraph.dnodes.*;
import org.choral.compiler.dependencygraph.role.Role;
import org.choral.compiler.dependencygraph.symboltable.Template;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GraphSolver implements DNodeVisitorInterface< Void > {

	private Template currentClass;
	private boolean expressionRoot = true;
	private final Set< Role > possibleUnfixedRoles = new HashSet<>();

	public static DRoot solve( DRoot root ){
		var gs = new GraphSolver();
		gs.visit( root );
		return root;
	}

	@Override
	public Void visit( DNode n ) {
		return n.accept( this );
	}

	@Override
	public Void visit( DClass n ) {
		currentClass = n.getClassTem();
		visit( n.getMethods() );
		return null;
	}

	@Override
	public Void visit( DMethod n ) {
		visit( n.getContent() );
		return null;
	}

	@Override
	public Void visit( DClassInstantiation n ) {
		expressionRoot = false;
		visitAll( n.getArguments() );

		Mapper.map( n.getArguments(), n.getParameters(), ( dNode, type ) -> {
			if( dNode.getType().getRoles().size() == 1 ){
				Role argRole = dNode.getType().getRoles().get( 0 ).getCanonicalRole();
				Role paramRole = type.getRoles().get( 0 ).getCanonicalRole();
				if( !argRole.isFixed() ){
					argRole.coalesce( paramRole );
					possibleUnfixedRoles.add( paramRole );
				}else if( !paramRole.isFixed() ){
					paramRole.coalesce( argRole );
					possibleUnfixedRoles.add( argRole );
				}
			}
			return dNode;
		} );

		return null;
	}

	@Override
	public Void visit( DExpression n ) {
		expressionRoot = false;
		visitAll( n.getDependencies() );
		return null;
	}

	@Override
	public Void visit( DAssign n ) {
		expressionRoot = false;
		visit( n.getTarget() );
		visit( n.getValue() );

		if( n.getTarget().getType().getRoles().size() == 1 ){
			assert n.getValue().getType().getRoles().size() == 1;
			Role valueRole = n.getValue().getType().getRoles().get( 0 ).getCanonicalRole();
			Role targetRole = n.getTarget().getType().getRoles().get( 0 ).getCanonicalRole();
			if( !valueRole.isFixed() ){
				valueRole.coalesce( targetRole );
			}else if( !targetRole.isFixed() ){
				targetRole.coalesce( valueRole );
			}
		}

		return null;
	}

	@Override
	public Void visit( DLiteral n ) {
		return null;
	}

	@Override
	public Void visit( DMethodCall n ) {
		expressionRoot = false;
		visitAll( n.getArguments() );

		Mapper.map( n.getArguments(), n.getParameters(), ( dNode, type ) -> {
					if( dNode.getType().getRoles().size() == 1 ){
						Role argRole = dNode.getType().getRoles().get( 0 ).getCanonicalRole();
						Role paramRole = type.getRoles().get( 0 ).getCanonicalRole();
						if( !argRole.isFixed() ){
							argRole.coalesce( paramRole );
							possibleUnfixedRoles.add( paramRole );
						}else if( !paramRole.isFixed() ){
							paramRole.coalesce( argRole );
							possibleUnfixedRoles.add( argRole );
						}
					}
					return dNode;
				} );

		return null;
	}

	@Override
	public Void visit( DReturn n ) {
		expressionRoot = false;
		visit( n.getReturnNode() );
		if( n.getReturnNode().getType().getRoles().size() == 1 ){
			Role role = n.getReturnNode().getType().getRoles().get( 0 ).getCanonicalRole();
			if( !role.isFixed() ){
				Role returnRole = n.getType().getRoles().get( 0 ).getCanonicalRole();
				role.coalesce( returnRole );
			}
		}

		return null;
	}

	@Override
	public Void visit( DStaticAccess n ) {
		return null;
	}

	@Override
	public Void visit( DThis n ) {
		return null;
	}

	@Override
	public Void visit( DVariable n ) {
		return null;
	}

	@Override
	public Void visit( DBinaryExpression n ) {
		boolean isRoot = expressionRoot;
		expressionRoot = false;
		visit( n.getLeft() );
		visit( n.getRight() );
		Role role = n.getType().getRoles().get( 0 ).getCanonicalRole();
		Role rightRole = n.getRight().getType().getRoles().get( 0 ).getCanonicalRole();
		Role leftRole = n.getLeft().getType().getRoles().get( 0 ).getCanonicalRole();

		if( !rightRole.isFixed() ){
			rightRole.coalesce( leftRole );
			role.coalesce( leftRole );
		}else if( !leftRole.isFixed() ){
			leftRole.coalesce( rightRole );
			role.coalesce( rightRole );
		}else if( rightRole == leftRole ){
			// both are fixed at the same role
			role.coalesce( rightRole );
		}else if( isRoot ){
			// If both are fixed at different roles,
			// leave role unless it is the root of the expression.
			role.coalesce( leftRole );
		}

		possibleUnfixedRoles.add( role );

		return null;
	}

	@Override
	public Void visit( DRoot n ) {
		possibleUnfixedRoles.clear();
		for( DNode node: n.getNodes() ){
			expressionRoot = true;
			visit( node );
			if( node.getType() != null && node.getType().getRoles().size() == 1 ) {
				var finalRole = node.getType().getRoles().get( 0 ).getCanonicalRole();

				if( !finalRole.isFixed() ) {
					// The role is not coalesced at this point, but may be in the future.
					// So it is saved for later
					possibleUnfixedRoles.add( finalRole );
				}
			}

		}

		for( Role role: possibleUnfixedRoles ){
			role = role.getCanonicalRole();
			// roles may have been fixed after being added to the set
			if( !role.isFixed() ){
				role.coalesce( currentClass.worldParameters().get( 0 ) );
			}
		}

		return null;
	}

	public void visitAll( List< DNode > nodes ){
		nodes.forEach( this::visit );
	}
}
