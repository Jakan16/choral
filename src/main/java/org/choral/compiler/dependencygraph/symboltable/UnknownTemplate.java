package org.choral.compiler.dependencygraph.symboltable;

import org.choral.ast.Name;
import org.choral.ast.body.Field;
import org.choral.ast.body.MethodDefinition;
import org.choral.ast.type.FormalTypeParameter;
import org.choral.ast.type.FormalWorldParameter;

import java.util.Collections;
import java.util.List;

public class UnknownTemplate extends Template {

	static final Template unknownTemplate = new UnknownTemplate();

	public static Template unknownTemplate() {
		return unknownTemplate;
	}

	private final List< FormalWorldParameter > worldParams;

	UnknownTemplate() {
		super( null, null );
		worldParams = Collections.singletonList( new FormalWorldParameter( new Name( "A" ) ) );
	}

	@Override
	public String getName() {
		return "UnknownType";
	}

	@Override
	public Field getField( String identifier ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List< ? extends MethodDefinition > getMethodDefs() {
		return Collections.emptyList();
	}

	@Override
	public List< FormalWorldParameter > worldParameters() {
		return worldParams;
	}

	@Override
	public List< FormalTypeParameter > typeParameters() {
		return Collections.emptyList();
	}
}
