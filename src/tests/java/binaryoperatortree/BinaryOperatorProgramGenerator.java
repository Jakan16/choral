package binaryoperatortree;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Random;

public class BinaryOperatorProgramGenerator {

	public static final String className = "RandomBinaryOperatorTree";
	public static final String destinationFolder = "build/generatedChoral/choral/";

	static {
		File directory = new File( destinationFolder );
		if (! directory.exists()){
			directory.mkdirs();
		}
	}

	public static String[] generateProgram( int size, int numRoles ){
		return generateProgram( size, numRoles, new Random(), new Random() );
	}

	public static String[] generateProgram( int size, int numRoles, Random treeRandom, Random sleepRandom ){

		BinaryOperatorTreeNode tree = BinaryOperatorTreeGenerator.generateTree( size, numRoles, treeRandom );
		String[] roles = BinaryOperatorTreeGenerator.getRoleNames( numRoles );

		try ( PrintWriter writer = new PrintWriter(new File(destinationFolder, className + ".ch"))) {
			writer.println("import choral.channels.SymChannel;");
			writer.println("import java.util.Random;");
			writer.print("public class " );
			writer.print( className );
			writer.print( "@(" );
			writer.print( String.join( ",", roles ) );
			writer.println( "){" );

			writer.print(channelDeclarations( roles, ";\n" ));
			writer.println(';');

			for( String s : roles ) {
				writer.print( "Random@" );
				writer.print( s );
				writer.print( " random_" );
				writer.print( s );
				writer.println( ";" );
			}

			writer.print( "public " );
			writer.print( className );
			writer.println( '(' );

			writer.print(channelDeclarations( roles, ",\n" ));
			writer.println();
			writer.println( "){" );

			for( int i = 0; i < roles.length; i++ ) {
				for( int j = i + 1; j < roles.length; j++ ) {
					String var_name = "ch_" + roles[i] + "_" + roles[j];
					writer.print( "this." );
					writer.print( var_name );
					writer.print( " = " );
					writer.print( var_name );
					writer.println( ';' );
				}
			}

			for( String s : roles ) {
				writer.print( "random_" );
				writer.print( s );
				writer.print( " = new Random(" );
				writer.print( sleepRandom.nextInt() );
				writer.println( ");" );
			}

			writer.println( '}' );

			writer.println( "public void run(){" );
			writer.println( "run2();" );
			writer.println( "run2();" );
			writer.println( "run2();" );
			writer.println( "run2();" );
			writer.println( "run2();" );
			writer.println( "run2();" );
			writer.println( "run2();" );
			writer.println( "run2();" );
			writer.println( "run2();" );
			writer.println( "run2();" );
			writer.println( "}" );

			writer.println( "public void run2(){" );
			writer.println( "run3();" );
			writer.println( "run3();" );
			writer.println( "run3();" );
			writer.println( "run3();" );
			writer.println( "run3();" );
			writer.println( "run3();" );
			writer.println( "run3();" );
			writer.println( "run3();" );
			writer.println( "run3();" );
			writer.println( "run3();" );
			writer.println( "}" );

			writer.println( "public void run3(){" );
			writer.println( "run4();" );
			writer.println( "run4();" );
			writer.println( "run4();" );
			writer.println( "run4();" );
			writer.println( "run4();" );
			writer.println( "run4();" );
			writer.println( "run4();" );
			writer.println( "run4();" );
			writer.println( "run4();" );
			writer.println( "run4();" );
			writer.println( "}" );

			writer.println( "public void run4(){" );
			writer.println( "tree();" );
			writer.println( "tree();" );
			writer.println( "tree();" );
			writer.println( "tree();" );
			writer.println( "tree();" );
			writer.println( "tree();" );
			writer.println( "tree();" );
			writer.println( "tree();" );
			writer.println( "tree();" );
			writer.println( "tree();" );
			writer.println( "}" );

			writer.println( "public void tree(){" );

			writer.print( "int@" );
			writer.print(roles[0]);
			writer.println( " num;" );

			writer.print( "num = " );
			writer.print( tree.toString() );
			writer.println( ';' );

			writer.println( "try {" );
			for( String role : roles ) {
				writer.print( "Thread@" );
				writer.print( role );
				writer.print( ".sleep(" );
				writer.print( "random_" );
				writer.print( role );
				writer.print( ".nextInt( 4 ) + 1" );
				writer.println( " );" );
			}
			/*

			for( int i = 0, rolesLength = roles.length; i < rolesLength; i++ ) {
				String role = roles[ i ];
				writer.print( "Thread@" );
				writer.print( role );
				writer.print( ".sleep(" );
				writer.print( i );
				writer.println( " );" );
			}
			*/
			writer.println('}');
			for( String role : roles ) {
				writer.print( "catch( Exception@" );
				writer.print( role );
				writer.println( " e ) {e.printStackTrace();}" );
			}

			writer.println('}');

			writer.println('}');

		} catch( FileNotFoundException e ) {
			e.printStackTrace();
		}

		return roles;
	}

	private static String channelDeclaration( String role1, String role2 ){
		return "SymChannel@(" + role1 + "," + role2 + ")< Object > ch_" + role1 + "_" + role2;
	}

	private static String channelDeclarations( String[] roles, String delimiter ){
		StringBuilder sb = new StringBuilder();
		for( int i = 0; i < roles.length; i++ ) {
			for( int j = i + 1; j < roles.length; j++ ) {
				if( i > 0 || j > 1 ){
					sb.append( delimiter );
				}

				sb.append( channelDeclaration( roles[i], roles[j] ) );
			}
		}
		return sb.toString();
	}
}
