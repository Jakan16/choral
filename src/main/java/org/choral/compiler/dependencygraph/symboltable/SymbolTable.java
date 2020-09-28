package org.choral.compiler.dependencygraph.symboltable;

import org.choral.compiler.dependencygraph.dnodes.TypeDNode;
import org.choral.compiler.dependencygraph.dnodes.VariableDNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SymbolTable {

	private final List< Scope > scopes;

	public SymbolTable(){
		scopes = new ArrayList<>();
	}

	public VariableDNode getSymbol( String identifier ){
		for( Scope scope: scopes ){
			VariableDNode node = scope.getSymbol( identifier );
			if( node != null ){
				return node;
			}
		}

		return null;
	}

	public void addSymbol( String identifier, List< String > roles, TypeDNode type ){
		assert getSymbol( identifier ) == null;
		currentScope().addSymbol( new VariableDNode( identifier, roles, type ) );
	}

	public void enterScope(){
		scopes.add( 0, new Scope() );
	}

	public void exitScope(){
		scopes.remove( 0 );
	}

	private Scope currentScope(){
		return scopes.get( 0 );
	}

	private static class Scope {
		Map< String, VariableDNode > symbols = new HashMap<>();

		VariableDNode getSymbol( String identifier ) {
			return symbols.get( identifier );
		}

		void addSymbol( VariableDNode node ) {
			symbols.put( node.getName(), node );
		}
	}
}
