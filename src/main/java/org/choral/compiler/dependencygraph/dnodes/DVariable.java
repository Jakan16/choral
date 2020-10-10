package org.choral.compiler.dependencygraph.dnodes;

import java.util.Collections;

/**
 * Node for a variable
 */
public class DVariable extends DNode {

	private final DType type;

	public DVariable( String name, DType type ) {
		super( Collections.emptyList(), name );
		this.type = type;
	}

	@Override
	public < R > R accept( DNodeVisitorInterface< R > v ) {
		return v.visit( this );
	}

	@Override
	public DType getType() {
		return type;
	}

	@Override
	public String toString() {
		return "VariableNode " + getName() + " " + type.toString();
	}
}
