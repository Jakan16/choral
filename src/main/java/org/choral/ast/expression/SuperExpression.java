/*
 * Copyright (C) 2019 by Saverio Giallorenzo <saverio.giallorenzo@gmail.com>
 * Copyright (C) 2019 by Fabrizio Montesi <famontesi@gmail.com>
 * Copyright (C) 2019 by Marco Peressotti <marco.peressotti@gmail.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Library General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this program; if not, write to the
 * Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.choral.ast.expression;

import org.choral.ast.Name;
import org.choral.ast.Node;
import org.choral.ast.Position;
import org.choral.ast.type.WorldArgument;
import org.choral.ast.visitors.ChoralVisitorInterface;
import org.choral.ast.visitors.MergerInterface;
import org.choral.exceptions.ChoralException;

import java.util.HashSet;
import java.util.Set;

/**
 * A wrapper for the invocation of `this`
 */

public class SuperExpression extends Expression {

	private Name aliasOf;

	public SuperExpression() {

	}

	public SuperExpression( final Position position ) {
		super( position );
	}

	public SuperExpression( Name aliasOf, final Position position ) {
		super( position );
		this.aliasOf = aliasOf;
	}

	public Name aliasOf() {
		return aliasOf;
	}

	public void setAliasOf( Name aliasOf ) {
		this.aliasOf = aliasOf;
	}

	// public static ThisExpression getNewInstance() {
	//    return new ThisExpression();
	// }

	private final Set< WorldArgument > epp_worlds = new HashSet<>();

	public Set< WorldArgument > epp_worlds() {
		return epp_worlds;
	}

	@Override
	public < R > R accept( ChoralVisitorInterface< R > v ) {
		return v.visit( this );
	}

	@Override
	public < R, T extends Node > R merge( MergerInterface< R > m, T n ) {
		try {
			return m.merge( this, ( this.getClass().cast( n ) ) );
		} catch( ClassCastException e ) {
			throw new ChoralException(
					"Could not merge " + this.getClass().getSimpleName() + " with " + n.getClass().getSimpleName() );
		}
	}

}
