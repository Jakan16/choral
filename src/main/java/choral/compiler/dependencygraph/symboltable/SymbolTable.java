package choral.compiler.dependencygraph.symboltable;

import choral.compiler.dependencygraph.dnodes.DType;
import choral.compiler.dependencygraph.dnodes.DVariable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

/**
 * Holds all variable and parameter declarations
 */
public class SymbolTable {

	private final Deque< Scope > scopes;

	public SymbolTable(){
		scopes = new ArrayDeque<>();
	}

	/**
	 * Returns a node for the variable if it is defined in the current or a parent scope.
	 * @param identifier the name of the variable
	 * @return The node for the identifier or {@code null} if it does not exist.
	 */
	public DVariable getSymbol( String identifier ){
		// Visit the current scope and travel to the root, until a definition is found,
		// returning null otherwise.
		for( Scope scope: scopes ){
			DVariable node = scope.getSymbol( identifier );
			if( node != null ){
				return node;
			}
		}

		return null;
	}

	/**
	 * Adds a new variable definition to the current scope.
	 * @param identifier The name of the variable
	 * @param type The type of the variable
	 * @return The {@link DVariable} representing the symbol added
	 */
	public DVariable addSymbol( String identifier, DType type ){
		if( getSymbol( identifier ) != null ){
			throw new IllegalStateException( "Variable " + identifier + " has already been declared" );
		}
		DVariable variableNode = new DVariable( identifier, type );
		currentScope().addSymbol( variableNode );
		return variableNode;
	}

	/**
	 * Creates a new scope for variables to be declared in
	 */
	public void enterScope(){
		scopes.addFirst( new Scope() );
	}

	/**
	 * Removes the current scope, deleting all definitions
	 */
	public void exitScope(){
		scopes.removeFirst();
	}

	private Scope currentScope(){
		return scopes.getFirst();
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
