package org.choral.compiler.dependencygraph.dnodes;

import org.choral.ast.Node;
import org.choral.ast.visitors.PrettyPrinterVisitor;

import java.util.Collections;
import java.util.List;

/**
 * Base class for dependency graph notes
 */
public abstract class DNode {

	private Node source;
	private final List< DNode > dependencies;
	private final String name;
	private DType resultingType;

	public DNode( List< DNode > dependencies, String name ) {
		this.dependencies = dependencies;
		this.name = name;
	}

	public List< DNode > getDependencies(){
		return dependencies;
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

	/**
	 * The resulting type of the completed expression
	 * @return The resulting type
	 */
	public DType getResultingType() {
		return resultingType;
	}

	/**
	 * Set the resulting type of the completed expression
	 * @param resultingType The resulting type
	 */
	public void setResultingType( DType resultingType ) {
		this.resultingType = resultingType;
	}

	public abstract < R > R accept( DNodeVisitorInterface< R > v );

	@Override
	public String toString() {
		if( getSource() != null ){
			return getSource().accept( new PrettyPrinterVisitor() );
		}
		return "";
	}
}
