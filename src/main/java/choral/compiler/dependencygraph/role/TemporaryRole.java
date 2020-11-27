package choral.compiler.dependencygraph.role;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class TemporaryRole extends Role {

	private Role parent;
	private Role parentIfPreferred;
	private List<Role> possibleRoles;
	private Set<Role> preferredRoles = Collections.emptySet();
	private boolean isPreferredUnion = false;

	@Override
	public void coalesce( Role coalesceTo ){
		assert parent == null;
		var role = coalesceTo.getCanonicalRole();
		if( this == role ){
			// coalescing a role to itself does nothing
			return;
		}
		parent = role;
	}

	@Override
	public void coalesceIfUnfixed( Role coalesceTo ) {
		coalesce( coalesceTo );
	}

	@Override
	public void coalesceIfPreferred( Role coalesceTo ) {
		if( this.isPreferredUnion ){
			// any role may be used, if preferred is made as an union
			// however the set may affect the parent, which this role is coalesced to.
			coalesce( coalesceTo );
		}else{
			assert parent == null;
			parentIfPreferred = coalesceTo;
		}
	}

	@Override
	public Role getCanonicalRole() {
		if( parent != null ) {
			Role fixedRole = parent.getCanonicalRole();
			// Shortcut to the root, for future calls
			parent = fixedRole;
			return fixedRole;
		}

		if( parentIfPreferred == null ){
			return this;
		}

		var maybeParent = parentIfPreferred.getCanonicalRole();
		assert maybeParent.isFixed();
		if( preferredRoles.contains( maybeParent ) ){
			coalesce( maybeParent );
		}else {
			coalesce( preferredRoles.iterator().next() );
		}
		return getCanonicalRole();
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
	public boolean isPreferredAUnion() {
		return this.isPreferredUnion;
	}

	@Override
	public void setPreferredAUnion( boolean isUnion ) {
		this.isPreferredUnion = isUnion;
	}

	@Override
	public String toString() {
		Role canonical = getCanonicalRole();
		if( canonical == this ){
			return String.valueOf( getNumber() );
		}
		return getNumber() + "->" + canonical.toString();
	}
}
