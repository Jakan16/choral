package org.choral.compiler.dependencygraph.dnodes;

import java.util.List;

public class MethodCallDNode extends DNode {

	private List<String> roles;
	private final TypeDNode returnType;

	public MethodCallDNode( List< DNode > dependencies, TypeDNode returnType ) {
		super( dependencies, "MethodCallDNode" );
		this.returnType = returnType;
	}

	public List<String> getRoles() {
		return roles;
	}

	public void setRole( List<String> roles) {
		this.roles = roles;
	}

	@Override
	public String toString() {
		if( getRoles() != null ){
			return getName() + " " + returnType.toString();
		}
		return getName();
	}

	@Override
	public TypeDNode getType() {
		return returnType;
	}
}
