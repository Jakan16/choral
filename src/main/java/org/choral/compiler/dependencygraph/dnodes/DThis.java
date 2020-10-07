package org.choral.compiler.dependencygraph.dnodes;

import java.util.Collections;

public class DThis extends DNode {

	public DThis() {
		super( Collections.emptyList(), "this" );
	}

	@Override
	public DType getType() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString() {
		return getName();
	}
}
