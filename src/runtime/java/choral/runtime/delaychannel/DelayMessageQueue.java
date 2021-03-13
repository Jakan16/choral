package choral.runtime.delaychannel;

public class DelayMessageQueue {

	private final Object[] circularBuffer;
	private final long[] circularTimeBuffer;
	private volatile int head = 0;
	private volatile int tail = 0;
	private final long latency;
	private final long processing;

	public DelayMessageQueue() {
		this(1024, 5*1000000, 5*1000000);
	}

	public DelayMessageQueue( int bufferSize, long latency, long processing ) {
		this.circularBuffer = new Object[ bufferSize ];
		this.circularTimeBuffer = new long[ bufferSize ];
		this.latency = latency;
		this.processing = processing;
	}

	void send( Object m ){

		if( processing > 0 ) {
			try {
				Thread.sleep( processing / 1000000, (int) ( processing % 1000000 ) );
			} catch( InterruptedException e ) {
				e.printStackTrace();
			}
		}

		int next = (tail+1)%circularBuffer.length;
		while( next == head ); // wait for consume
		circularBuffer[ tail ] = m;
		circularTimeBuffer[ tail ] = System.nanoTime();
		tail = next;
	}

	Object receive(){
		while( head == tail ); // wait for produce
		Object val = circularBuffer[ head ];
		long wait = circularTimeBuffer[ head ];
		while( System.nanoTime() - wait <= latency );
		head = (head+1)%circularBuffer.length;

		return val;
	}

}
