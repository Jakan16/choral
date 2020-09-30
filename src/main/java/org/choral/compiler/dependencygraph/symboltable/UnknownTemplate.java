package org.choral.compiler.dependencygraph.symboltable;

import org.choral.ast.Name;
import org.choral.ast.type.FormalTypeParameter;
import org.choral.ast.type.FormalWorldParameter;
import org.choral.compiler.dependencygraph.dnodes.VariableDNode;

import java.util.Collections;
import java.util.List;

public class UnknownTemplate extends Template {

	static final Template unknownTemplate = new UnknownTemplate();

	public static Template unknownTemplate() {
		return unknownTemplate;
	}

	private final List< FormalWorldParameter > worldParams;

	UnknownTemplate() {
		super( null, null, Collections.emptyList() );
		worldParams = Collections.singletonList( new FormalWorldParameter( new Name( "A" ) ) );
	}

	@Override
	public String getName() {
		return "UnknownType";
	}

	@Override
	public List< VariableDNode > getFields() {
		throw new UnsupportedOperationException();
	}

	@Override
	public VariableDNode getField( String identifier ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List< MethodSignature > getMethodSigs() {
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
