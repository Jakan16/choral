package org.choral.compiler.dependencygraph.dnodes;

import java.util.List;

/**
 * Node for generic expression
 */
public class DExpression extends DNode{

	private final List< DNode > dependencies;

	public DExpression( List< DNode > dependencies ) {
		super( "ExpressionDNode" );
		this.dependencies = dependencies;
	}

	public DExpression( List< DNode > dependencies, String name ) {
		super( name );
		this.dependencies = dependencies;
	}

	@Override
	public DType getType() {
		return this.dependencies.get( this.dependencies.size() - 1 ).getType();
	}

	public List< DNode > getDependencies() {
		return dependencies;
	}

	@Override
	public < R > R accept( DNodeVisitorInterface< R > v ) {
		return v.visit( this );
	}

	@Override
	public String toString() {
		return getName() + " " + getType();
	}
}
