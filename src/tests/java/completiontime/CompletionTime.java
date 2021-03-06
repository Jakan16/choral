package completiontime;

import binaryoperatortree.BinaryOperatorProgramGenerator;
import org.junit.Test;
import runtimecompiler.RuntimeCompiler;
import selections.SelectionsProgramGenerator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class CompletionTime {

	@Test
	public void randomTrees() throws Throwable {

		int size = 64;
		int numRoles = 8;

		long seed = 28980;
		Random randomTree = new Random( seed );
		Random randomSleep = new Random( seed );

		String filename = "leaf_count_wait.txt";
		String path = "src/tests/python/binarytreealgoanalyzer/execution_times";
		File directory = new File( path );
		if (! directory.exists()){
			directory.mkdirs();
		}
		PrintWriter writer = new PrintWriter( new File( path, filename ) );

		long[] sum = new long[numRoles];
		int total = 100;
		for( int i = 0; i < total; i++ ) {
			System.out.printf("completed %d/%d trees\n", i, total);

			String[] roles = BinaryOperatorProgramGenerator.generateProgram( size, numRoles, randomTree, randomSleep );

			RuntimeCompiler compiler = RuntimeCompiler.compile(
					BinaryOperatorProgramGenerator.destinationFolder,
					BinaryOperatorProgramGenerator.className,
					Arrays.asList( roles )
			);

			long[] executionTime = compiler.invokeMethod( "run3" ).assertNoErrors().getExecutionTime();
			for( int j = 0; j < sum.length; j++ ) {
				sum[j] += executionTime[j];
			}

			writer.println( Arrays.stream( executionTime ).mapToObj( String::valueOf ).collect( Collectors.joining( ", ", "[", "]" ) ) );
		}

		for( long l : sum ) {
			System.out.println( ( ( l / total ) / 1000000 ) + " ms" );
		}

		writer.close();
	}

	@Test
	public void selections() throws Throwable {
		long seed = 28980;
		Random randomSleep = new Random( seed );

		int numRoles = 7;

		String filename = "selections_4_no_wait.txt";
		String path = "src/tests/python/selections/execution_times";
		File directory = new File( path );
		if (! directory.exists()){
			directory.mkdirs();
		}
		PrintWriter writer = new PrintWriter( new File( path, filename ) );

		long[] sum = new long[numRoles];
		int total = 1;
		for( int i = 0; i < total; i++ ) {
			System.out.printf("completed %d/%d iterations\n", i, total);

			String[] roles = SelectionsProgramGenerator.generateProgram( numRoles, randomSleep );
			RuntimeCompiler compiler = RuntimeCompiler.compile(
					SelectionsProgramGenerator.destinationFolder,
					SelectionsProgramGenerator.className,
					Arrays.asList( roles )
			);

			long[] executionTime = compiler.invokeMethod( "selection" ).assertNoErrors().getExecutionTime();
			for( int j = 0; j < sum.length; j++ ) {
				sum[j] += executionTime[j];
			}

			writer.println( Arrays.stream( executionTime ).mapToObj( String::valueOf ).collect( Collectors.joining( ", ", "[", "]" ) ) );
		}

		for( long l : sum ) {
			//System.out.println( ( ( l / total ) / 1000000 ) + " ms" );
			System.out.println( ( l / total ) + " ns" );
		}

		writer.close();
	}

}
