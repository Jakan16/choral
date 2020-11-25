package choral.compiler.dependencygraph.role;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class TemporaryRole extends Role {

	private Role parent;
	private List<Role> possibleRoles;
	private Set<Role> preferredRoles = Collections.emptySet();

	@Override
	public void coalesce( Role coalesceTo ){
		assert parent == null;
		if( this == coalesceTo.getCanonicalRole() ){
			// coalescing a role to itself does nothing
			return;
		}
		parent = coalesceTo;
	}

	@Override
	public void coalesceIfUnfixed( Role coalesceTo ) {
		coalesce( coalesceTo );
	}

	@Override
	public Role getCanonicalRole() {
		if( parent == null ){
			return this;
		}
		Role fixedRole = parent.getCanonicalRole();
		// Shortcut to the root, for future calls
		parent = fixedRole;
		return fixedRole;
	}

	@Override
	public String getName() {
		Role canonical = getCanonicalRole();
		if( canonical == this ){
			if( this.possibleRoles != null && this.possibleRoles.size() > 0 ){
				return this.possibleRoles.get( 0 ).getName();
			}
			throw new IllegalStateException( "Temporary not coalesced to fixed role has no name" );
		}
		return canonical.getName();
	}

	@Override
	public boolean isFixed() {
		if( parent == null ){
			return false;
		}
		return getCanonicalRole().isFixed();
	}

	@Override
	public void setPossibleRoles( List< Role > role ) {
		this.possibleRoles = role;
	}

	@Override
	public List< Role > getPossibleRoles() {
		return this.possibleRoles;
	}

	@Override
	public Set< Role > getPreferredRoles() {
		return this.preferredRoles;
	}

	@Override
	public void setPreferredRoles( Set<Role> roles ) {
		this.preferredRoles = roles;
	}

	@Override
	public String toString() {
		Role canonical = getCanonicalRole();
		if( canonical == this ){
			return String.valueOf( getNumber() );
		}
		return canonical.toString();
	}
}
