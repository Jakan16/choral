package choral.runtime.delaychannel;

import choral.channels.SymChannel_B;

public class DelayChannel_B extends DelayChannel implements SymChannel_B< Object > {
	public DelayChannel_B( DelayMessageQueue queueOut, DelayMessageQueue queueIn ) {
		super( queueOut, queueIn );
	}
}
