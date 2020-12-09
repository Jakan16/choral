
import choral.channels.SymChannel;
import java.util.*;

public class Host@( A, B ) {

    public Host( SymChannel@( A, B )< Object > ch_A_B ){
        // Channel not needed for this test
    }

    public String@A test( ){
        Class@( B, A )< String, ArrayList< String >, List< String > > genericClass = new Class@( B, A )< String, ArrayList< String >, List< String > >( "hello" );

        ArrayList< String > stringList = new ArrayList< String >();
        stringList.add( "Forty-two" );
        String unpacked = genericClass.func( stringList );

        genericClass.hash( "Some text" );

        return genericClass.getVal();
    }

}