package compile;

import org.junit.Test;
import runtimecompiler.RuntimeCompiler;

import java.util.*;
import java.util.function.Function;

import static runtimecompiler.RuntimeCompiler.*;

public class Programs {

	@Test
	public void mergesort() throws Throwable {

		RuntimeCompiler instance = RuntimeCompiler.compile(
				"src/tests/choral/tests/mergesort",
				"Mergesort",
				Arrays.asList( A, B, C )
		);

		Random random = new Random( 15 );
		List< Integer > numbers = new ArrayList<>();

		for( int i = 0; i < 200; i++ ) {
			numbers.add( i );
		}

		List< Integer > shuffled = new ArrayList<>( numbers );

		for( int i = 0; i < 10; i++ ) {
			Collections.shuffle( shuffled, random );

			instance.method( "sort" )
					.argAt( A, shuffled )
					.invoke()
					.assertNoErrors()
					.assertEqualAt( A, numbers );
		}
	}

	@Test
	public void quicksort() throws Throwable {

		RuntimeCompiler instance = RuntimeCompiler.compile(
				"src/tests/choral/tests/quicksort",
				"Quicksort",
				Arrays.asList( A, B, C )
		);

		Random random = new Random( 22 );
		List< Integer > numbers = new ArrayList<>();

		for( int i = 0; i < 200; i++ ) {
			numbers.add( i );
		}

		for( int i = 0; i < 10; i++ ) {
			List< Integer > shuffled = new ArrayList<>( numbers );
			Collections.shuffle( shuffled, random );

			instance.method( "sort" )
					.argAt( A, shuffled )
					.invoke()
					.assertNoErrors()
					.assertEqualAt( A, numbers );
		}
	}

	@Test
	public void remoteFunction() throws Throwable {
		String client = "Client";
		String server = "Server";
		RuntimeCompiler instance = RuntimeCompiler.compile(
				"src/tests/choral/tests/remotefunction",
				"RemoteFunction",
				Arrays.asList( client, server )
		);

		Function< Integer, Integer > cube = (num) -> num*num*num;

		int intVal = 5;
		instance.method( "call" )
				.argAt( client, intVal )
				.argAt( server, cube ).invoke()
				.assertEqualAt( client, cube.apply( intVal ) );

		intVal = 10;
		instance.method( "call" )
				.argAt( client, intVal )
				.argAt( server, cube ).invoke()
				.assertEqualAt( client, cube.apply( intVal ) );

		String val = "Hello";
		instance.method( "call" )
				.argAt( client, val )
				.argAt( server, (Function< String, Integer >) String::length ).invoke()
				.assertEqualAt( client, val.length() );
	}
}
