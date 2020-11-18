package choral.compiler.dependencygraph.symboltable;

import choral.compiler.dependencygraph.dnodes.DType;
import choral.compiler.dependencygraph.dnodes.DVariable;
import choral.compiler.dependencygraph.role.FixedRole;
import choral.compiler.dependencygraph.role.Role;

import java.util.Collections;
import java.util.List;

/**
 * This {@link Template} represents and unknown time, and is assumed to be over a single role with
 * no fields or methods.
 */
public class UnknownTemplate extends Template {

	static final Template unknownTemplate = new UnknownTemplate();

	public static Template unknownTemplate() {
		return unknownTemplate;
	}

	@Override
	public List< DType > prepareSuperType() {
		return Collections.emptyList();
	}

	private final List< Role > worldParams;

	UnknownTemplate() {
		super( null, null, Collections.emptyList() );
		worldParams = Collections.singletonList( new FixedRole( "A" ) );
	}

	@Override
	public String getName() {
		return "UnknownType";
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
	public List< MethodSig > getMethodSigs() {
		return Collections.emptyList();
	}

	@Override
	public List< Role > worldParameters() {
		return worldParams;
	}

	@Override
	public List< String > typeParameters() {
		return Collections.emptyList();
	}
}
