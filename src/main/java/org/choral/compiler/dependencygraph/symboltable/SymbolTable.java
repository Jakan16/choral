package org.choral.compiler.dependencygraph.symboltable;

import org.choral.compiler.dependencygraph.dnodes.DType;
import org.choral.compiler.dependencygraph.dnodes.DVariable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SymbolTable {

	private final List< Scope > scopes;

	public SymbolTable(){
		scopes = new ArrayList<>();
	}

	public DVariable getSymbol( String identifier ){
		for( Scope scope: scopes ){
			DVariable node = scope.getSymbol( identifier );
			if( node != null ){
				return node;
			}
		}

		return null;
	}

	public void addSymbol( String identifier, DType type ){
		assert getSymbol( identifier ) == null;
		currentScope().addSymbol( new DVariable( identifier, type ) );
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
		Map< String, DVariable > symbols = new HashMap<>();

		DVariable getSymbol( String identifier ) {
			return symbols.get( identifier );
		}

		void addSymbol( DVariable node ) {
			symbols.put( node.getName(), node );
		}
	}
}
