/*
 *     Copyright (C) 2019 by Saverio Giallorenzo <saverio.giallorenzo@gmail.com>
 *     Copyright (C) 2019 by Fabrizio Montesi <famontesi@gmail.com>
 *     Copyright (C) 2019 by Marco Peressotti <marco.peressotti@gmail.com>
 *
 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of the GNU Library General Public License as
 *     published by the Free Software Foundation; either version 2 of the
 *     License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU Library General Public
 *     License along with this program; if not, write to the
 *     Free Software Foundation, Inc.,
 *     59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package choral.ast.statement;

import choral.ast.Node;
import choral.ast.Position;
import choral.ast.expression.Expression;
import choral.ast.visitors.ChoralVisitorInterface;
import choral.ast.visitors.MergerInterface;
import choral.ast.visitors.PrettyPrinterVisitor;
import choral.exceptions.ChoralException;

/**
 * if( e ) { statement } else { statement }; continuation
 */

public class IfStatement extends Statement {

	private final Expression condition;
	private final Statement ifBranch, elseBranch;

	public IfStatement(
			final Expression condition, final Statement ifBranch, final Statement elseBranch,
			final Statement continuation
	) {
		super( continuation );
		this.condition = condition;
		this.ifBranch = ifBranch;
		this.elseBranch = elseBranch;
	}

	public IfStatement(
			final Expression condition, final Statement ifBranch, final Statement elseBranch,
			final Statement continuation, final Position position
	) {
		super( continuation, position );
		this.condition = condition;
		this.ifBranch = ifBranch;
		this.elseBranch = elseBranch;
	}

	public Expression condition() {
		return condition;
	}

	public Statement ifBranch() {
		return ifBranch;
	}

	public Statement elseBranch() {
		return elseBranch;
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
			if( this.position() != null ) {
				throw new ChoralException(
						this.position().line() + ":"
								+ this.position().column() + ":"
								+ "error: Could not merge \n" + new PrettyPrinterVisitor().visit(
								this ) + "\n with " + n.getClass().getSimpleName() );
			}else{
				throw new ChoralException("error: Could not merge \n" +
						new PrettyPrinterVisitor().visit( this ) +
						"\n with " + n.getClass().getSimpleName() );
			}
		}
	}

	@Override
	public Statement cloneWithContinuation( Statement continuation ) {
		return new IfStatement(
				this.condition(),
				this.ifBranch(),
				this.elseBranch(),
				this.continuation() == null ? continuation : continuation().cloneWithContinuation(
						continuation ),
				this.position() );
	}
}
