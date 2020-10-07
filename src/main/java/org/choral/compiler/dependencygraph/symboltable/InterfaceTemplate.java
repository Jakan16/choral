package org.choral.compiler.dependencygraph.symboltable;

import org.choral.ast.ImportDeclaration;
import org.choral.ast.body.Field;
import org.choral.ast.body.Interface;
import org.choral.ast.body.MethodDefinition;
import org.choral.ast.type.FormalTypeParameter;
import org.choral.ast.type.FormalWorldParameter;
import org.choral.compiler.dependencygraph.dnodes.DType;
import org.choral.compiler.dependencygraph.dnodes.DVariable;

import java.util.Collections;
import java.util.List;

public class InterfaceTemplate extends Template {
	private final Interface interfaceNode;

	InterfaceTemplate( Interface interfaceNode, List< ImportDeclaration > importDeclarations, Package holdingPackage ) {
		super( importDeclarations, holdingPackage, interfaceNode.typeParameters() );
		this.interfaceNode = interfaceNode;
	}

	@Override
	public List< DType > prepareSuperType() {
		return typeExpressionsToNodes( interfaceNode.extendsInterfaces() );
	}

	@Override
	public String getName() {
		return interfaceNode.name().identifier();
	}

	@Override
	public List< DVariable > getFields() {
		throw new UnsupportedOperationException();
	}

	@Override
	public DVariable getField( String identifier ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List< FormalWorldParameter > worldParameters() {
		return interfaceNode.worldParameters();
	}

	@Override
	public List< FormalTypeParameter > typeParameters() {
		return interfaceNode.typeParameters();
	}

	@Override
	public List< ? extends MethodDefinition > methodDefinitions() {
		return interfaceNode.methods();
	}

	@Override
	public List< Field > fields() {
		return Collections.emptyList();
	}
}
