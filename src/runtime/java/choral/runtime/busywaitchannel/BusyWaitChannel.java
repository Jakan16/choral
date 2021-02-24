package choral.runtime.busywaitchannel;

import choral.channels.SymChannelImpl;
import choral.lang.Unit;

public class BusyWaitChannel implements SymChannelImpl< Object > {

	private final BusyMessageQueue queueOut;
	private final BusyMessageQueue queueIn;

	public BusyWaitChannel(
			BusyMessageQueue queueOut, BusyMessageQueue queueIn
	) {
		this.queueOut = queueOut;
		this.queueIn = queueIn;
	}

	@Override
	public < S > Unit com( S m ) {
		queueOut.send( m );
		return Unit.id;
	}

	@Override
	public < S > S com( Unit m ) {
		return (S) queueIn.receive();
	}

	@Override
	public < S > S com() {
		return (S) queueIn.receive();
	}

	@Override
	public < T extends Enum< T > > Unit select( T m ) {
		queueOut.send( m );
		return Unit.id;
	}

	@Override
	public < T extends Enum< T > > T select( Unit m ) {
		return (T) queueIn.receive();
	}

	@Override
	public < T extends Enum< T > > T select() {
		return (T) queueIn.receive();
	}
}
