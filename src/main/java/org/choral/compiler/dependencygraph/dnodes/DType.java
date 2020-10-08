package org.choral.compiler.dependencygraph.dnodes;

import org.choral.compiler.dependencygraph.Mapper;
import org.choral.compiler.dependencygraph.symboltable.Template;

import java.util.List;
import java.util.Map;

/**
 * Represents a type with roles and type arguments
 */
public class DType {

	private final Template tem;
	private final List< String > roles;
	private final List< DType > typeArguments;

	public DType( Template tem, List< String > roles, List< DType > typeArguments ) {
		this.tem = tem;
		this.roles = roles;
		this.typeArguments = typeArguments;
	}

	/**
	 * Returns a copy with the roles mapped using the given map
	 * @param roleMap Mapping of roles from one world to another
	 * @return A copy of this type, with mapped roles
	 */
	public DType copyWithMapping( Map< String, String > roleMap ){
		List< String > roles = Mapper.map( this.roles, roleMap::get );
		assert !roles.contains( null );
		return new DType( this.tem, roles, this.typeArguments );
	}

	public Template getTem() {
		return tem;
	}

	public String getName(){
		return tem.getName();
	}

	public List< String > getRoles() {
		return roles;
	}

	public List< DType > getTypeArguments() {
		return typeArguments;
	}

	@Override
	public String toString() {
		return getName() + "@(" + String.join( ", ", roles ) + ")";
	}
}
