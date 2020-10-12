package org.choral.compiler.dependencygraph.symboltable;

import org.choral.ast.type.FormalTypeParameter;
import org.choral.ast.type.TypeExpression;
import org.choral.compiler.dependencygraph.Mapper;
import org.choral.compiler.dependencygraph.dnodes.DType;
import org.choral.compiler.dependencygraph.role.FixedRole;
import org.choral.compiler.dependencygraph.role.Role;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class GenericTemplate extends Template {

	private final FormalTypeParameter parameter;
	private final Template parentTemplate;
	private Map< String, Template > funcGenericsMap = Collections.emptyMap();
	private final List< Role > roles;

	GenericTemplate( Template parentTemplate, FormalTypeParameter parameter ) {
		super( Collections.emptyList(), parentTemplate.getHoldingPackage(), Collections.emptyList() );
		this.parentTemplate = parentTemplate;
		this.parameter = parameter;
		this.roles = Mapper.map( parameter.worldParameters(),
				w -> new FixedRole( w.toWorldArgument().name().identifier() ) );
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
		return Collections.singletonList( typeExpressionToDType( parameter.upperBound().get( 0 ) ) );
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
	protected DType typeExpressionToDTypeWithParentRoles( TypeExpression typeExpression ) {
		return typeExpressionToDType( typeExpression, parentTemplate.getRoleIdentifierMap() );
	}

	@Override
	public String getName() {
		return parameter.name().identifier();
	}

	@Override
	public List< Role > worldParameters() {
		return roles;
	}

	@Override
	public List< String > typeParameters() {
		return Collections.emptyList();
	}

}
