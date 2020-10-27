package org.choral.compiler.dependencygraph.dnodes;

/**
 * A static access of a class
 */
public class DStaticAccess extends DNode {

	private final DType type;

	public DStaticAccess( DType type ) {
		super( "Static Access" );
		this.type = type;
	}

	@Override
	public < R > R accept( DNodeVisitorInterface< R > v ) {
		return v.visit( this );
	}

	@Override
	public DType getType() {
		return this.type;
	}

	@Override
	public String toString() {
		return type.toString() + " " + super.toString();
	}
}
