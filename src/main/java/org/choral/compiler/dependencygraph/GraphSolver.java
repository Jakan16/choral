package org.choral.compiler.dependencygraph;

import org.choral.compiler.dependencygraph.dnodes.*;
import org.choral.compiler.dependencygraph.role.Role;
import org.choral.compiler.dependencygraph.role.TemporaryRole;

import java.util.List;

public class GraphSolver implements DNodeVisitorInterface< Void > {

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
	public Void visit( DClassInstantiation n ) {
		visitAll( n.getArguments() );
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
		return null;
	}

	@Override
	public Void visit( DLiteral n ) {
		return null;
	}

	@Override
	public Void visit( DMethodCall n ) {
		visitAll( n.getArguments() );
		return null;
	}

	@Override
	public Void visit( DReturn n ) {
		visit( n.getReturnNode() );
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
		visit( n.getLeft() );
		visit( n.getRight() );
		Role role = n.getType().getRoles().get( 0 );
		if( !role.isFixed() ){
			(( TemporaryRole ) role).coalesce( n.getRight().getType().getRoles().get( 0 ) );
		}
		return null;
	}

	@Override
	public Void visit( DRoot n ) {
		visitAll( n.getNodes() );
		return null;
	}

	public void visitAll( List< DNode > nodes ){
		nodes.forEach( this::visit );
	}
}
