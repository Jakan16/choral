package org.choral.compiler.dependencygraph.dnodes;

import org.choral.compiler.dependencygraph.symboltable.Template;

public class DClass extends DNode {

	private final Template classTem;
	private final DNode methods;

	public DClass( Template classTem, DNode methods ) {
		super( classTem.getName() );
		this.classTem = classTem;
		this.methods = methods;
	}

	public Template getClassTem() {
		return classTem;
	}

	public DNode getMethods() {
		return methods;
	}

	@Override
	public DType getType() {
		return null;
	}

	@Override
	public < R > R accept( DNodeVisitorInterface< R > v ) {
		return v.visit( this );
	}

	@Override
	public String toString() {
		return getName() + " " + super.toString();
	}
}
