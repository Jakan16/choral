package org.choral.compiler.dependencygraph.dnodes;

import org.choral.ast.expression.BinaryExpression;

import java.util.List;

public class DBinaryExpression extends DNode {

	private final BinaryExpression.Operator operator;

	public DBinaryExpression( List< DNode > dependencies, BinaryExpression.Operator operator ) {
		super( dependencies, "BinaryExpression" );
		this.operator = operator;
	}

	@Override
	public DType getType() {
		return getDependencies().get( getDependencies().size()-1 ).getType();
	}

	@Override
	public < R > R accept( DNodeVisitorInterface< R > v ) {
		return v.visit( this );
	}

	@Override
	public String toString() {
		return getName() + " " + operator + " " + getType();
	}
}
