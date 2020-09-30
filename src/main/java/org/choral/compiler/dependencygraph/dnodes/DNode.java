package org.choral.compiler.dependencygraph.dnodes;

import org.choral.ast.Node;
import org.choral.ast.visitors.PrettyPrinterVisitor;

import java.util.List;

public abstract class DNode {

	private Node source;
	private final List< DNode > dependencies;
	private final String name;

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

	public abstract TypeDNode getType();

	@Override
	public String toString() {
		if( getSource() != null ){
			return getSource().accept( new PrettyPrinterVisitor() );
		}
		return "";
	}
}