package compile;

import org.junit.Test;
import runtimecompiler.RuntimeCompiler;

import java.util.Arrays;
import java.util.List;

import static runtimecompiler.Assert.assertEqual;
import static runtimecompiler.Assert.assertNoErrors;

public class Tests {

	@Test
	public void hello() throws Throwable {
		RuntimeCompiler compiler = new RuntimeCompiler(
				"src/tests/choral/tests/hello",
				"HelloRoles",
				Arrays.asList( "A", "B" )
		).compile();
		compiler.invokeMethod( "sayHello" );
	}

	@Test
	public void addition() throws Throwable {
		RuntimeCompiler compiler = new RuntimeCompiler(
				"src/tests/choral/tests/addition",
				"Addition",
				Arrays.asList( "A", "B", "C" )
		).compile();

		List< Object > results = compiler.invokeMethod( "add" );

		assertNoErrors( results );
		assertEqual( results, 30, 0 );
	}

	@Test
	public void assign() throws Throwable {
		RuntimeCompiler compiler = new RuntimeCompiler(
				"src/tests/choral/tests/assign",
				"Assign",
				Arrays.asList( "A", "B", "C" )
		).compile();

		List< Object > results = compiler.invokeMethod( "assign" );
		assertNoErrors( results );
	}

	@Test
	public void ifs() throws Throwable {
		RuntimeCompiler compiler = new RuntimeCompiler(
				"src/tests/choral/tests/if",
				"If",
				Arrays.asList( "A", "B", "C" )
		).compile();

		List< Object > results = compiler.invokeMethod( "test" );
		assertNoErrors( results );
	}
}
