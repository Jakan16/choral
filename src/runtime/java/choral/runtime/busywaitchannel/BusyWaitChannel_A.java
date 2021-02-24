package choral.runtime.busywaitchannel;

import choral.channels.SymChannel_A;

public class BusyWaitChannel_A extends BusyWaitChannel implements SymChannel_A< Object > {
	public BusyWaitChannel_A( BusyMessageQueue queueOut, BusyMessageQueue queueIn ) {
		super( queueOut, queueIn );
	}
}
