package choral.runtime.delaychannel;

import choral.channels.SymChannel_A;

public class DelayChannel_A extends DelayChannel implements SymChannel_A< Object > {
	public DelayChannel_A( DelayMessageQueue queueOut, DelayMessageQueue queueIn ) {
		super( queueOut, queueIn );
	}
}
