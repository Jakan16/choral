package org.choral.compiler.dependencygraph.symboltable;

import org.choral.ast.body.Field;
import org.choral.ast.body.MethodDefinition;
import org.choral.compiler.dependencygraph.dnodes.DType;

import java.util.Collections;
import java.util.List;

/**
 * Represents a Java primitive type
 */
public class PrimitiveTemplate extends Template {

	private final Template boxedType;

	PrimitiveTemplate( Template boxedType ) {
		// the Holding package does not need to be set,
		// as all methods are delegated to the boxedType of the primitive
		super( Collections.emptyList(), null, Collections.emptyList() );

		this.boxedType = boxedType;
	}

	@Override
	public String getName() {
		return boxedType.getName();
	}

	@Override
	public List< DType > prepareSuperType() {
		return boxedType.prepareSuperType();
	}

	@Override
	public List< String > worldParameters() {
		return boxedType.worldParameters();
	}

	@Override
	public List< String > typeParameters() {
		return boxedType.typeParameters();
	}

	@Override
	public List< ? extends MethodDefinition > methodDefinitions() {
		return boxedType.methodDefinitions();
	}

	@Override
	public List< Field > fields() {
		return boxedType.fields();
	}
}