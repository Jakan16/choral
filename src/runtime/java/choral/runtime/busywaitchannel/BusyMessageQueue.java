package choral.runtime.busywaitchannel;

public class BusyMessageQueue {

	private final Object[] circularBuffer;
	private volatile int head = 0;
	private volatile int tail = 0;

	public BusyMessageQueue() {
		this.circularBuffer = new Object[1024];
	}

	public BusyMessageQueue( int bufferSize ) {
		this.circularBuffer = new Object[ bufferSize ];
	}

	void send( Object m ){
		int next = (tail+1)%circularBuffer.length;
		while( next == head ); // wait for consume
		circularBuffer[ tail ] = m;
		tail = next;
	}

	Object receive(){
		while( head == tail ); // wait for produce
		Object val = circularBuffer[ head ];
		head = (head+1)%circularBuffer.length;
		return val;
	}

}
