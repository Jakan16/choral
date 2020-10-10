package org.choral.compiler.dependencygraph.dnodes;

import java.util.Collections;

/**
 * A static access of a class
 */
public class DStaticAccess extends DNode {

	private final DType type;

	public DStaticAccess( DType type ) {
		super( Collections.emptyList(), "Static Access" );
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
		return type.toString();
	}
}
