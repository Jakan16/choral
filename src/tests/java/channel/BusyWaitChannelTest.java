package channel;

import choral.channels.SymChannel_A;
import choral.channels.SymChannel_B;
import org.junit.Test;
import choral.runtime.busywaitchannel.BusyMessageQueue;
import choral.runtime.busywaitchannel.BusyWaitChannel_A;
import choral.runtime.busywaitchannel.BusyWaitChannel_B;

public class BusyWaitChannelTest {

	@Test
	public void stressTest() throws InterruptedException {

		BusyMessageQueue ab = new BusyMessageQueue();
		BusyMessageQueue ba = new BusyMessageQueue();

		SymChannel_A< Object > channel_a = new BusyWaitChannel_A( ab, ba );
		SymChannel_B< Object > channel_b = new BusyWaitChannel_B( ba, ab );

		final int iterations = 100000;

		Thread producer = new Thread( () -> {
			for( int i = 0; i < iterations; i++ ) {
				channel_a.com( i );
			}
		} );

		Thread consumer = new Thread( () -> {
			for( int i = 0; i < iterations; i++ ) {
				assert channel_b.com().equals( i );
			}
		} );

		producer.start();
		Thread.sleep( 100 );
		consumer.start();

		consumer.join();
	}

}
