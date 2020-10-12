package org.choral.compiler.dependencygraph.dnodes;

/**
 * Node for a variable
 */
public class DVariable extends DNode {

	private final DType type;

	public DVariable( String name, DType type ) {
		super( name );
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
