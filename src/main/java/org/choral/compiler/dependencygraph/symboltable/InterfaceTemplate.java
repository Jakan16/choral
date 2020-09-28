package org.choral.compiler.dependencygraph.symboltable;

import org.choral.ast.ImportDeclaration;
import org.choral.ast.body.Field;
import org.choral.ast.body.Interface;
import org.choral.ast.body.MethodDefinition;
import org.choral.ast.type.FormalTypeParameter;
import org.choral.ast.type.FormalWorldParameter;

import java.util.List;

public class InterfaceTemplate extends Template {
	private final Interface interfaceNode;

	InterfaceTemplate( Interface interfaceNode, List< ImportDeclaration > importDeclarations, Package holdingPackage ) {
		super( importDeclarations, holdingPackage );
		this.interfaceNode = interfaceNode;
	}

	@Override
	public String getName() {
		return interfaceNode.name().identifier();
	}

	@Override
	public Field getField( String identifier ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List< ? extends MethodDefinition > getMethodDefs() {
		return interfaceNode.methods();
	}

	@Override
	public List< FormalWorldParameter > worldParameters() {
		return interfaceNode.worldParameters();
	}

	@Override
	public List< FormalTypeParameter > typeParameters() {
		return interfaceNode.typeParameters();
	}
}
