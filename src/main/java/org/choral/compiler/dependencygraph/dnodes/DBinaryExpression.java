package org.choral.compiler.dependencygraph.dnodes;

import org.choral.ast.expression.BinaryExpression;

public class DBinaryExpression extends DNode {

	private final BinaryExpression.Operator operator;
	private final DNode left;
	private final DNode right;
	private final DType resultType;

	public DBinaryExpression(
			DNode left,
			DNode right,
			BinaryExpression.Operator operator,
			DType resultType
	) {
		super( "BinaryExpression" );
		this.operator = operator;
		this.left = left;
		this.right = right;
		this.resultType = resultType;
	}

	public DNode getLeft() {
		return left;
	}

	public DNode getRight() {
		return right;
	}

	@Override
	public DType getType() {
		return resultType;
	}

	@Override
	public < R > R accept( DNodeVisitorInterface< R > v ) {
		return v.visit( this );
	}

	@Override
	public String toString() {
		return getName() + " " + operator + " " + getType() + " " + super.toString();
	}
}
