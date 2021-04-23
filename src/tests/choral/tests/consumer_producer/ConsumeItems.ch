import choral.channels.SymChannel;
import java.util.function.Consumer;
import java.util.Iterator;

public class ConsumeItems@( A, B ) {

    private SymChannel@( A, B )< Object > ch_A_B;

    public ConsumeItems(SymChannel@( A, B )< Object > ch_A_B){
        this.ch_A_B = ch_A_B;
    }

	public void consumeItems( Iterator@A< Integer > it, Consumer@B< Integer > consumer ){
		if ( it.hasNext() ){
			it.next() >> consumer::accept;
			consumeItems( it, consumer );
		}
    }
}