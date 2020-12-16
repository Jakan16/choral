package compile;

import org.junit.Test;
import runtimecompiler.RuntimeCompiler;

import java.util.Arrays;
import java.util.Collections;

import static runtimecompiler.RuntimeCompiler.*;

public class Tests {

	@Test
	public void hello() throws Throwable {
		RuntimeCompiler.compile(
				"src/tests/choral/tests/hello",
				"HelloRoles",
				Arrays.asList( A, B )
		).invokeMethod( "sayHello" );
	}

	@Test
	public void addition() throws Throwable {
		RuntimeCompiler.compile(
				"src/tests/choral/tests/addition",
				"Addition",
				Arrays.asList( A, B, C )
		).invokeMethod( "add" )
				.assertNoErrors()
				.assertEqualAt( A, 30 );
	}

	@Test
	public void assign() throws Throwable {
		RuntimeCompiler.compile(
				"src/tests/choral/tests/assign",
				"Assign",
				Arrays.asList( A, B, C )
		).invokeMethod( "assign" ).assertNoErrors();
	}

	@Test
	public void ifs() throws Throwable {
		RuntimeCompiler.compile(
				"src/tests/choral/tests/if",
				"If",
				Arrays.asList( A, B, C )
		).invokeMethod( "test" ).assertNoErrors();
	}

	@Test
	public void packages() throws Throwable {
		RuntimeCompiler.compile(
				"src/tests/choral/tests/packages",
				"Root",
				Collections.singletonList( A )
		).invokeMethod( "sum" ).assertNoErrors()
		.assertEqualAt( A, 20 );
	}

	@Test
	public void fieldAccess() throws Throwable {
		RuntimeCompiler.compile(
				"src/tests/choral/tests/fieldaccess",
				"FieldAccess",
				Collections.singletonList( A )
		).invokeMethod( "sum" ).assertNoErrors()
				.assertEqualAt( A, 27 );
	}

	@Test
	public void autobox() throws Throwable {
		RuntimeCompiler.compile(
				"src/tests/choral/tests/autobox",
				"AutoBoxing",
				Arrays.asList( A, B )
		).invokeMethod( "f" ).assertNoErrors();
	}

	@Test
	public void generics() throws Throwable {
		RuntimeCompiler.compile(
				"src/tests/choral/tests/generic",
				"Host",
				Arrays.asList( A, B )
		).invokeMethod( "test" ).assertNoErrors().assertEqualAt( A, "hello" );
	}

	@Test
	public void extension() throws Throwable {
		RuntimeCompiler instance = RuntimeCompiler.compile(
				"src/tests/choral/tests/extension",
				"ExtensionClass",
				Arrays.asList( A, B )
		);
		instance.invokeMethod( "getVal" ).assertNoErrors().assertEqualAt( B, "Text" );
		instance.invokeMethod( "getValTwice" ).assertNoErrors().assertEqualAt( B, Arrays.asList( "Text", " ", "Text" ) );
	}
}
