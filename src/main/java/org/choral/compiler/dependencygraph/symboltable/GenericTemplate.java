package org.choral.compiler.dependencygraph.symboltable;

import org.choral.ast.type.FormalTypeParameter;
import org.choral.ast.type.FormalWorldParameter;
import org.choral.compiler.dependencygraph.dnodes.VariableDNode;

import java.util.Collections;
import java.util.List;

public class GenericTemplate extends Template {

	private static Template objectTemplate;
	private final String name;

	GenericTemplate( Package root, String name ) {
		super( Collections.emptyList(), root.getRoot().getPackage( "java.lang" ), Collections.emptyList() );
		this.name = name;
		if( objectTemplate == null ) {
			objectTemplate = getHoldingPackage().getTemplate( "Object" );
		}
	}

	@Override
	public boolean isGeneric() {
		return true;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public List< VariableDNode > getFields() {
		return objectTemplate.getFields();
	}

	@Override
	public VariableDNode getField( String identifier ) {
		return objectTemplate.getField( identifier );
	}

	@Override
	public List< MethodSignature > getMethodSigs() {
		return objectTemplate.getMethodSigs();
	}

	@Override
	public MethodSignature getMethodSig( String identifier, int numArgs ) {
		return objectTemplate.getMethodSig( identifier, numArgs );
	}

	@Override
	public List< FormalWorldParameter > worldParameters() {
		return objectTemplate.worldParameters();
	}

	@Override
	public List< FormalTypeParameter > typeParameters() {
		return objectTemplate.typeParameters();
	}

}
