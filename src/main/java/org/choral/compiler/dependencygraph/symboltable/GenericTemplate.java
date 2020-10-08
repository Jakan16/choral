package org.choral.compiler.dependencygraph.symboltable;

import org.choral.ast.body.Field;
import org.choral.ast.body.MethodDefinition;
import org.choral.ast.type.FormalTypeParameter;
import org.choral.compiler.dependencygraph.Mapper;
import org.choral.compiler.dependencygraph.dnodes.DType;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class GenericTemplate extends Template {

	private final FormalTypeParameter parameter;
	private final Template parentTemplate;
	private Map< String, Template > funcGenericsMap = Collections.emptyMap();

	GenericTemplate( Template parentTemplate, FormalTypeParameter parameter ) {
		super( Collections.emptyList(), parentTemplate.getHoldingPackage(), Collections.emptyList() );
		this.parentTemplate = parentTemplate;
		this.parameter = parameter;
	}

	public void setFuncGenericsMap(	Map< String, Template > funcGenericsMap	) {
		this.funcGenericsMap = funcGenericsMap;
	}

	@Override
	public boolean isGeneric() {
		return true;
	}

	@Override
	public List< DType > prepareSuperType() {
		if( parameter.upperBound().isEmpty() ){
			return Collections.emptyList();
		}
		assert parameter.upperBound().size() == 1;
		return Collections.singletonList( typeExpressionToNode( parameter.upperBound().get( 0 ) ) );
	}

	@Override
	public Template resolveIdentifier( String identifier ) {

		Template tem = funcGenericsMap.get( identifier );
		if( tem != null ){
			return tem;
		}

		return parentTemplate.resolveIdentifier( identifier );
	}

	@Override
	public String getName() {
		return parameter.name().identifier();
	}

	@Override
	public List< String > worldParameters() {
		return Mapper.map( parameter.worldParameters(),
				w -> w.toWorldArgument().name().identifier() );
	}

	@Override
	public List< String > typeParameters() {
		return Collections.emptyList();
	}

}
