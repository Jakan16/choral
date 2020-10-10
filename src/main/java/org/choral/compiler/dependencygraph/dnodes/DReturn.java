package org.choral.compiler.dependencygraph.dnodes;

import java.util.List;

/**
 * Node for return statement, holds the return type of the method it is returning in
 */
public class DReturn extends DNode {

	private final DType returnType;

	public DReturn( List< DNode > dependencies, DType returnType ) {
		super( dependencies, "return" );
		this.returnType = returnType;
	}

	@Override
	public < R > R accept( DNodeVisitorInterface< R > v ) {
		return v.visit( this );
	}

	@Override
	public DType getType() {
		return returnType;
	}

	@Override
	public String toString() {
		return getName() + " " + returnType.toString();
	}
}
