package choral.compiler.dependencygraph;

import choral.compiler.dependencygraph.dnodes.*;
import choral.compiler.dependencygraph.role.Role;
import choral.compiler.dependencygraph.role.RoleHierarchy;
import choral.compiler.dependencygraph.symboltable.Template;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GraphSolver implements DNodeVisitorInterface< Void > {

	private Template currentClass;
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
		visitAll( n.getArguments() );

		Mapper.map( n.getArguments(), n.getParameters(), ( dNode, type ) -> {
			if( dNode.getType().getRoles().size() == 1 ){
				Role argRole = dNode.getType().getRoles().get( 0 ).getCanonicalRole();
				Role paramRole = type.getRoles().get( 0 ).getCanonicalRole();
				if( !paramRole.isFixed() ){
					paramRole.coalesce( argRole );
					possibleUnfixedRoles.add( argRole );
				}else if( !argRole.isFixed() ){
					argRole.coalesce( paramRole );
					possibleUnfixedRoles.add( paramRole );
				}
			}
			return dNode;
		} );

		return null;
	}

	@Override
	public Void visit( DExpression n ) {
		visitAll( n.getDependencies() );
		return null;
	}

	@Override
	public Void visit( DAssign n ) {
		visit( n.getTarget() );
		visit( n.getValue() );

		if( n.getTarget().getType().getRoles().size() == 1 ){
			assert n.getValue().getType().getRoles().size() == 1;
			Role valueRole = n.getValue().getType().getRoles().get( 0 ).getCanonicalRole();
			Role targetRole = n.getTarget().getType().getRoles().get( 0 ).getCanonicalRole();
			if( !targetRole.isFixed() ){
				targetRole.coalesce( valueRole );
			}else if( !valueRole.isFixed() ){
				valueRole.coalesce( targetRole );
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
		visitAll( n.getArguments() );

		Mapper.map( n.getArguments(), n.getParameters(), ( dNode, type ) -> {
					if( dNode.getType().getRoles().size() == 1 ){
						Role argRole = dNode.getType().getRoles().get( 0 ).getCanonicalRole();
						Role paramRole = type.getRoles().get( 0 ).getCanonicalRole();
						if( !paramRole.isFixed() ){
							paramRole.coalesce( argRole );
							possibleUnfixedRoles.add( argRole );
						}else if( !argRole.isFixed() ){
							argRole.coalesce( paramRole );
							possibleUnfixedRoles.add( paramRole );
						}
					}
					return dNode;
				} );

		return null;
	}

	@Override
	public Void visit( DReturn n ) {
		if( n.getReturnNode() == null ){
			return null;
		}
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
		//minCom( n );
		//minComPreferred( n );
		hierarchy( n );
		//moveRight( n );
		//sendDirect( n );
		return null;
	}

	private RoleHierarchy roleHierarchy;

	private void hierarchy( DBinaryExpression n ){
		Role role = n.getType().getRoles().get( 0 ).getCanonicalRole();
		boolean resetHierarchy = false;
		if(roleHierarchy == null){
			roleHierarchy = new RoleHierarchy();
			resetHierarchy = true;
			roleHierarchy.setRole( role );
		}
		roleHierarchy.goLeft();
		visit( n.getLeft() );
		roleHierarchy.goUp();
		roleHierarchy.goRight();
		visit( n.getRight() );
		roleHierarchy.goUp();

		Role leftRole = n.getLeft().getType().getRoles().get( 0 ).getCanonicalRole();
		Role rightRole = n.getRight().getType().getRoles().get( 0 ).getCanonicalRole();

		roleHierarchy.setLeftRole( leftRole );
		roleHierarchy.setRightRole( rightRole );

		if( rightRole.getUnion().isEmpty() ) {
			// right is not bound anywhere, bubble left
			role.coalesce( leftRole );
			rightRole.coalesce( leftRole );
		} else if( leftRole.getUnion().isEmpty() ){
			// left is not bound anywhere, bubble right
			role.coalesce( rightRole );
			leftRole.coalesce( rightRole );
		}else{
			if( resetHierarchy ){
				role.hierarchyAlert( roleHierarchy );
			}

			var union = new HashSet<>( rightRole.getUnion() );
			union.addAll( leftRole.getUnion() );
			role.setUnion( union );
			role.setLeftUnion( leftRole.getUnion() );
			role.setRightUnion( rightRole.getUnion() );

			if( union.size() == 1 ){
				// if only one role in subtree, coalesce to it.
				role.coalesce( union.iterator().next() );
			}

			leftRole.coalesceHierarchical( role, roleHierarchy );
			rightRole.coalesceHierarchical( role, roleHierarchy );
		}

		possibleUnfixedRoles.add( role );
		if( resetHierarchy ){
			roleHierarchy = null;
		}
	}

	private void minCom( DBinaryExpression n ){
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
		}

		possibleUnfixedRoles.add( role );
	}

	private void minComPreferred( DBinaryExpression n ){
		visit( n.getLeft() );
		visit( n.getRight() );
		Role role = n.getType().getRoles().get( 0 ).getCanonicalRole();
		Role rightRole = n.getRight().getType().getRoles().get( 0 ).getCanonicalRole();
		Role leftRole = n.getLeft().getType().getRoles().get( 0 ).getCanonicalRole();

		if( rightRole.getPreferredRoles().isEmpty() ) {
			// right is not bound anywhere, bubble left
			role.coalesce( leftRole );
			rightRole.coalesce( leftRole );
		} else if( leftRole.getPreferredRoles().isEmpty() ){
			// left is not bound anywhere, bubble right
			role.coalesce( rightRole );
			leftRole.coalesce( rightRole );
		}else{
			var intersection = new HashSet<>( rightRole.getPreferredRoles() );
			intersection.retainAll( leftRole.getPreferredRoles() );

			if( intersection.isEmpty() ){
				// if intersection is empty, use union.
				var preferredRoles = new HashSet<>( rightRole.getPreferredRoles() );
				preferredRoles.addAll( leftRole.getPreferredRoles() );
				role.setPreferredRoles( preferredRoles );
				role.setPreferredAUnion( true );
			}else if( intersection.size() == 1 ){
				// if only one intersects, coalesce to it.
				role.coalesce( intersection.iterator().next() );
			}else { // intersection.size() > 1
				// if intersection is multiple, use it
				role.setPreferredRoles( intersection );
				role.setPreferredAUnion( false );
			}

			leftRole.coalesceIfPreferred( role );
			rightRole.coalesceIfPreferred( role );
		}

		possibleUnfixedRoles.add( role );
	}

	private void moveRight( DBinaryExpression n ){
		visit( n.getLeft() );
		visit( n.getRight() );
		Role role = n.getType().getRoles().get( 0 ).getCanonicalRole();
		Role rightRole = n.getRight().getType().getRoles().get( 0 ).getCanonicalRole();
		Role leftRole = n.getLeft().getType().getRoles().get( 0 ).getCanonicalRole();

		role.coalesce( leftRole );
		rightRole.coalesceIfUnfixed( leftRole );
		possibleUnfixedRoles.add( role );
	}

	private void sendDirect( DBinaryExpression n ){
		visit( n.getLeft() );
		visit( n.getRight() );
		Role role = n.getType().getRoles().get( 0 ).getCanonicalRole();
		Role rightRole = n.getRight().getType().getRoles().get( 0 ).getCanonicalRole();
		Role leftRole = n.getLeft().getType().getRoles().get( 0 ).getCanonicalRole();

		if( leftRole == rightRole ){ // both fixed at the same role
			role.coalesce( leftRole );
		}else{
			rightRole.coalesceIfUnfixed( role );
			leftRole.coalesceIfUnfixed( role );
		}
	}

	@Override
	public Void visit( DRoot n ) {
		possibleUnfixedRoles.clear();
		for( DNode node: n.getNodes() ){
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
				role.setPossibleRoles( currentClass.worldParameters() );
			}
		}

		return null;
	}

	public void visitAll( List< DNode > nodes ){
		nodes.forEach( this::visit );
	}
}
