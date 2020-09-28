package org.choral.compiler.dependencygraph.dnodes;

import java.util.Collections;

public class ThisDNode extends DNode {

	public ThisDNode() {
		super( Collections.emptyList(), "this" );
	}

	@Override
	public TypeDNode getType() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString() {
		return getName();
	}
}
