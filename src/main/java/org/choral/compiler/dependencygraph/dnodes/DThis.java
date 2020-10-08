package org.choral.compiler.dependencygraph.dnodes;

import java.util.Collections;

/**
 * Node for super and this keyword.
 * Does not have roles or a type,
 * and is not needed for the final dependency graph, but is used to manage context.
 */
public class DThis extends DNode {

	public DThis() {
		super( Collections.emptyList(), "this or super" );
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
