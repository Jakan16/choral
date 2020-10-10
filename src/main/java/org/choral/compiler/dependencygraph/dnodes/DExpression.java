package org.choral.compiler.dependencygraph.dnodes;

import java.util.List;

/**
 * Node for generic expression
 */
public class DExpression extends DNode{

	public DExpression( List< DNode > dependencies ) {
		super( dependencies, "ExpressionDNode" );
	}

	public DExpression( List< DNode > dependencies, String name ) {
		super( dependencies, name );
	}

	@Override
	public DType getType() {
		return getDependencies().get( getDependencies().size()-1 ).getType();
	}

	@Override
	public < R > R accept( DNodeVisitorInterface< R > v ) {
		return v.visit( this );
	}

	@Override
	public String toString() {
		return getName() + " " + getResultingType();
	}
}
