package compile;

import org.junit.Test;
import runtimecompiler.RuntimeCompiler;

import java.util.Arrays;

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
}
