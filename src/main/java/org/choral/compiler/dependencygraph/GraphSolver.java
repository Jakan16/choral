package org.choral.compiler.dependencygraph;

import org.choral.compiler.dependencygraph.dnodes.*;

import java.util.Collections;
import java.util.List;

public class GraphSolver implements DNodeVisitorInterface< Void > {

	public static List< DNode > solve( List< DNode > roots ){

		var gs = new GraphSolver();
		roots.forEach( gs::visit );

		return roots;
	}

	@Override
	public Void visit( DNode n ) {
		return null;
	}

	@Override
	public Void visit( DClassInstantiation n ) {
		return null;
	}

	@Override
	public Void visit( DExpression n ) {
		return null;
	}

	@Override
	public Void visit( DLiteral n ) {
		return null;
	}

	@Override
	public Void visit( DMethodCall n ) {
		return null;
	}

	@Override
	public Void visit( DReturn n ) {
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
		return null;
	}

	@Override
	public Void visit( DRoot n ) {
		return null;
	}

	/*@Override
	public Void visit( DNode n ) {
		return n.accept( this );
	}

	@Override
	public Void visit( DClassInstantiation n ) {
		visitAll( n.getDependencies() );
		n.setResultingType( n.getType() );
		return null;
	}

	@Override
	public Void visit( DExpression n ) {
		visitAll( n.getDependencies() );
		n.setResultingType( n.getDependencies().get( n.getDependencies().size() - 1 ).getResultingType() );
		return null;
	}

	@Override
	public Void visit( DLiteral n ) {
		visitAll( n.getDependencies() );
		n.setResultingType( n.getType() );
		return null;
	}

	@Override
	public Void visit( DMethodCall n ) {
		visitAll( n.getDependencies() );
		n.setResultingType( n.getType() );
		return null;
	}

	@Override
	public Void visit( DReturn n ) {
		visitAll( n.getDependencies() );
		n.setResultingType( n.getType() );
		return null;
	}

	@Override
	public Void visit( DStaticAccess n ) {
		visitAll( n.getDependencies() );
		n.setResultingType( n.getType() );
		return null;
	}

	@Override
	public Void visit( DThis n ) {
		throw new IllegalStateException();
	}

	@Override
	public Void visit( DVariable n ) {
		visitAll( n.getDependencies() );
		n.setResultingType( n.getType() );
		return null;
	}

	@Override
	public Void visit( DBinaryExpression n ) {
		visitAll( n.getDependencies() );

		DType type1 = n.getDependencies().get( 0 ).getResultingType();
		DType type2 = n.getDependencies().get( 1 ).getResultingType();

		DType resultingType = new DType( type1.getTem(), type1.getRoles(), Collections.emptyList() );
		n.setResultingType( resultingType );

		return null;
	}

	@Override
	public Void visit( DRoot n ) {
		visitAll( n.getDependencies() );
		return null;
	}

	public void visitAll( List< DNode > nodes ){
		nodes.forEach( n -> n.accept( this ) );
	}*/
}
