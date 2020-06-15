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

package org.choral.compiler.soloist;

import org.choral.ast.Name;
import org.choral.ast.Node;
import org.choral.ast.body.*;
import org.choral.ast.statement.NilStatement;
import org.choral.ast.type.TypeExpression;
import org.choral.ast.type.WorldArgument;
import org.choral.ast.visitors.AbstractSoloistProjector;
import org.choral.compiler.unitNormaliser.StatementsUnitNormaliser;
import org.choral.compiler.unitNormaliser.UnitRepresentation;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class BodyProjector extends AbstractSoloistProjector< Node > {

	private BodyProjector( WorldArgument w ) {
		super( w );
	}

	static < T extends Node > List< T > visitAndCollect( WorldArgument w, List< T > n ) {
		return new BodyProjector( w ).visitAndCollect( n );
	}

	@Override
	public Field visit( Field n ) {
		if( n.typeExpression().worldArguments().contains( this.world() ) ) {
			return new Field(
					n.name(),
					TypesProjector.visit( this.world(), n.typeExpression() ).get( 0 ),
					n.modifiers(),
					n.position()
			);
		} else {
			return null; // these are removed by the visitAndCollect method
		}
	}

	@Override
	public MethodSignature visit( MethodSignature n ) {
		return new MethodSignature(
				n.name(),
				TypesProjector.visitAndCollect( this.world(), n.typeParameters() ),
				n.parameters().stream().map( this::unit ).collect( Collectors.toList() ),
				Utils.ifPresent( TypesProjector.visit( world(), n.returnType() ) )
						.getOrElse( () ->
								new TypeExpression(
										UnitRepresentation.UNIT,
										Collections.singletonList( this.world() ),
										Collections.emptyList()
								)
						),
				n.position()
		);
	}

	@Override
	public ConstructorSignature visit( ConstructorSignature n ) {
		return new ConstructorSignature(
				new Name( Utils.getProjectionName( n.name().identifier(), this.world(),
						n.typeAnnotation().get().declarationContext().worldArguments().stream()
								.map( w -> new WorldArgument( new Name( w.identifier() ) ) )
								.collect( Collectors.toList() )
				) ),
				TypesProjector.visitAndCollect( this.world(), n.typeParameters() ),
				n.parameters().stream()
						.map( this::unit )
						.collect( Collectors.toList() ),
				n.position()
		);
	}

	@Override
	public Node visit( Annotation n ) {
		return new Annotation( n.getName(), n.getValues() ).copyPosition( n );
	}

	// NOT USED ANYMORE, REMOVE IN FUTURE VERSIONS
	@Override
	public CaseSignature visit( CaseSignature n ) {
		return new CaseSignature(
				n.name(),
				TypesProjector.visitAndCollect( world(), n.parameters() )
		).copyPosition( n );
	}

	@Override
	public ClassMethodDefinition visit( ClassMethodDefinition n ) {
		ClassMethodDefinition m = new ClassMethodDefinition(
				visit( n.signature() ),
				n.body().isPresent() ?
						StatementsUnitNormaliser.visitStatement(
								StatementsProjector.visit( this.world(), n.body().get() ) )
						: null,
				visitAndCollect( n.annotations() ),
				n.modifiers(),
				n.position()
		);
		return m;
	}

	@Override
	public InterfaceMethodDefinition visit( InterfaceMethodDefinition n ) {
		InterfaceMethodDefinition m = new InterfaceMethodDefinition(
				visit( n.signature() ),
				n.annotations(),
				n.modifiers(),
				n.position()
		);
		return m;
	}


	@Override
	public ConstructorDefinition visit( ConstructorDefinition n ) {
		ConstructorDefinition c = new ConstructorDefinition(
				visit( n.signature() ),
				StatementsUnitNormaliser.visitStatement(
						StatementsProjector.visit( this.world(), n.body() ) ),
				n.modifiers(),
				n.position()
		);
		return c;
	}

	private FormalMethodParameter unit( FormalMethodParameter p ) {
		List< FormalMethodParameter > l = TypesProjector.visit( this.world(), p );
		return l.isEmpty() ?
				new FormalMethodParameter(
						p.name(),
						new TypeExpression( UnitRepresentation.UNIT,
								Collections.singletonList( this.world() ),
								Collections.emptyList() ),
						p.position()
				)
				: l.get( 0 );
	}

	private < T extends Node > List< T > visitAndCollect( List< T > n ) {
		return n.stream().map( this::safeVisit ).filter( Objects::nonNull ).collect(
				Collectors.toList() );
	}

	@SuppressWarnings( "unchecked cast" )
	private < T extends Node > T safeVisit( T n ) {
		return (T) n.accept( this );
	}

}
