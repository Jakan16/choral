package org.choral.compiler.dependencygraph.dnodes;

import org.choral.ast.Node;
import org.choral.ast.visitors.PrettyPrinterVisitor;

import java.util.Collections;

/**
 * Base class for dependency graph notes
 */
public abstract class DNode {

	private Node source;
	private final String name;

	public DNode( String name ) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public Node getSource() {
		return source;
	}

	public void setSource( Node source ) {
		this.source = source;
	}

	public DRoot merge( DNode other ){
		return new DRoot( Collections.singletonList( this ) ).merge( other );
	}

	/**
	 * The type to the extent it is known from the source code
	 * @return the known non-inferred type
	 */
	public abstract DType getType();

	public abstract < R > R accept( DNodeVisitorInterface< R > v );

	@Override
	public String toString() {
		if( getSource() != null ){
			return getSource().accept( new PrettyPrinterVisitor() );
		}
		return "";
	}
}
