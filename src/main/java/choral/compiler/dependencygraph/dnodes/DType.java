package choral.compiler.dependencygraph.dnodes;

import choral.ast.Name;
import choral.ast.type.TypeExpression;
import choral.compiler.dependencygraph.Mapper;
import choral.compiler.dependencygraph.role.Role;
import choral.compiler.dependencygraph.symboltable.Template;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represents a type with roles and type arguments
 */
public class DType {

	private final Template tem;
	private final List< Role > roles;
	private final List< DType > typeArguments;

	public DType( Template tem, List< Role > roles, List< DType > typeArguments ) {
		this.tem = tem;
		this.roles = roles;
		this.typeArguments = typeArguments;
	}

	/**
	 * Returns a copy with the roles mapped using the given map
	 * @param roleMap Mapping of roles from one world to another
	 * @return A copy of this type, with mapped roles
	 */
	public DType copyWithMapping( Map< Role, Role > roleMap ){
		List< Role > roles = Mapper.map( this.roles, roleMap::get );
		assert !roles.contains( null );
		return new DType( this.tem, roles, this.typeArguments );
	}

	public Template getTem() {
		return tem;
	}

	public String getName(){
		return tem.getName();
	}

	public List< Role > getRoles() {
		return roles;
	}

	public List< DType > getTypeArguments() {
		return typeArguments;
	}

	@Override
	public String toString() {
		return getName() +
				"@(" +
				roles.stream().map( Object::toString ).collect( Collectors.joining(", ")) +
				")";
	}

	/**
	 * Creates an AST representation for this type, without roles.
	 * @return The {@link TypeExpression} without roles, for this type
	 */
	public TypeExpression toTypeExpression(){
		return new TypeExpression( new Name( getName() ), Collections.emptyList(),
				Mapper.map( getTypeArguments(),	DType::toTypeExpression ) );
	}
}
