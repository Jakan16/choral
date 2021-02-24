package choral.runtime.busywaitchannel;

import choral.channels.SymChannel_B;

public class BusyWaitChannel_B extends BusyWaitChannel implements SymChannel_B< Object > {
	public BusyWaitChannel_B( BusyMessageQueue queueOut, BusyMessageQueue queueIn ) {
		super( queueOut, queueIn );
	}
}
