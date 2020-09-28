package org.choral.compiler.dependencygraph.dnodes;

import org.choral.compiler.dependencygraph.symboltable.Template;

import java.util.Collections;
import java.util.List;

public class TypeDNode extends DNode{

	private final Template tem;
	private final List< String > roles;
	private final List< TypeDNode > typeArguments;

	public TypeDNode( Template tem, List< String > roles, List< TypeDNode > typeArguments ) {
		super(null, tem.getName() );
		this.tem = tem;
		this.roles = roles;
		this.typeArguments = typeArguments;
	}

	public Template getTem() {
		return tem;
	}

	public List< String > getRoles() {
		return roles;
	}

	public List< TypeDNode > getTypeArguments() {
		return typeArguments;
	}

	@Override
	public List< DNode > getDependencies() {
		return Collections.emptyList();
	}

	@Override
	public TypeDNode getType() {
		return this;
	}

	@Override
	public String toString() {
		return getName() + "@(" + String.join( ", ", roles ) + ") " + super.toString();
	}
}
