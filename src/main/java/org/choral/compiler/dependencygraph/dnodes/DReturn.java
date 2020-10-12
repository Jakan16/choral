package org.choral.compiler.dependencygraph.dnodes;

/**
 * Node for return statement, holds the return type of the method it is returning in
 */
public class DReturn extends DNode {

	private final DType returnType;
	private final DNode returnNode;

	public DReturn( DNode returnNode, DType returnType ) {
		super( "return" );
		this.returnType = returnType;
		this.returnNode = returnNode;
	}

	@Override
	public < R > R accept( DNodeVisitorInterface< R > v ) {
		return v.visit( this );
	}

	@Override
	public DType getType() {
		return returnType;
	}

	public DNode getReturnNode() {
		return returnNode;
	}

	@Override
	public String toString() {
		return getName() + " " + returnType.toString();
	}
}
