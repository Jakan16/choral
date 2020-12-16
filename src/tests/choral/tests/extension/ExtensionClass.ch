import choral.channels.SymChannel;
import java.util.*;

public class ExtensionClass@( A, B ) extends BaseClass@( A, B )< String > {

    public ExtensionClass( SymChannel@( A, B )< Object > ch_A_B ){
        //super( "Text"@B );
        // Channel not needed for this test
        setVal( "Text" );
    }

    public List@B< String > getValTwice(){
        ArrayList< String > list = new ArrayList< String >();
        list.add( value );
        list.add( " " );
        list.add( value );

        return list;
    }
}