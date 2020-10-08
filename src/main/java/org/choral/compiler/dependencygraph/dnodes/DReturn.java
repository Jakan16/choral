package org.choral.compiler.dependencygraph.dnodes;

import java.util.List;

public class DReturn extends DNode {

	private final DType returnType;

	public DReturn( List< DNode > dependencies, DType returnType ) {
		super( dependencies, "return" );
		this.returnType = returnType;
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
