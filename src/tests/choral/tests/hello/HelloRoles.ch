import choral.channels.SymChannel;

public class HelloRoles@( A, B ) {

    SymChannel@( A, B )< Object > ch_A_B;

    public HelloRoles(SymChannel@( A, B )< Object > ch_A_B){
        this.ch_A_B = ch_A_B;
    }

	public void sayHello() {
		String@A a = "Hello from A";
    	String@A b = "Hello from B";
		System.out.println( a );
		System@B.out.println( b );
	}
}
