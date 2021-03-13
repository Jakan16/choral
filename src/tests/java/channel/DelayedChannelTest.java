package channel;

import choral.channels.SymChannel_A;
import choral.channels.SymChannel_B;
import choral.runtime.delaychannel.DelayChannel_A;
import choral.runtime.delaychannel.DelayChannel_B;
import choral.runtime.delaychannel.DelayMessageQueue;
import org.junit.Test;

public class DelayedChannelTest {

	@Test
	public void stressTest() throws InterruptedException {

		DelayMessageQueue ab = new DelayMessageQueue(1024, 1000, 0);
		DelayMessageQueue ba = new DelayMessageQueue(1024, 1000, 0);

		SymChannel_A< Object > channel_a = new DelayChannel_A( ab, ba );
		SymChannel_B< Object > channel_b = new DelayChannel_B( ba, ab );

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

	@Test
	public void simpleTimeTest(){
		DelayMessageQueue ab = new DelayMessageQueue(5, 5000000000L, 5000000000L); // 10 secs
		DelayMessageQueue ba = new DelayMessageQueue(5, 5000000000L, 5000000000L); // 10 secs

		SymChannel_A< Object > channel_a = new DelayChannel_A( ab, ba );
		SymChannel_B< Object > channel_b = new DelayChannel_B( ba, ab );

		long start = System.nanoTime();
		channel_a.com( "hej" );
		channel_b.com();
		assert System.nanoTime() - start > 10000000000L - 5;
	}

	@Test
	public void timeTest() throws InterruptedException {
		DelayMessageQueue ab = new DelayMessageQueue(2, 1000000000L,0); // 1 sec
		DelayMessageQueue ba = new DelayMessageQueue(2, 1000000000L,0); // 1 sec

		SymChannel_A< Object > channel_a = new DelayChannel_A( ab, ba );
		SymChannel_B< Object > channel_b = new DelayChannel_B( ba, ab );

		final int iterations = 100;

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

		long start = System.nanoTime();
		producer.start();
		consumer.start();

		consumer.join();

		assert System.nanoTime() - start > 100000000000L - 5; // 100 secs
	}
}
