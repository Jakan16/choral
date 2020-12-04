import choral.channels.SymChannel;

public class Assign@( A, B, C ) {

    SymChannel@( A, B )< Object > ch_A_B;
    SymChannel@( A, C )< Object > ch_A_C;
    SymChannel@( B, C )< Object > ch_B_C;

    public Assign(SymChannel@( A, B )< Object > ch_A_B, SymChannel@( A, C )< Object > ch_A_C, SymChannel@( B, C )< Object > ch_B_C){
        this.ch_A_B = ch_A_B;
        this.ch_A_C = ch_A_C;
        this.ch_B_C = ch_B_C;
    }

	public void assign() {
		int@A intPrim = 4;
        Integer@A integerObj = 3;

        boolean@B boolPrim = true;
        Boolean@B boolObj = false;

        long@C longPrim = 3;
        //Long@C longerObj = 70L; // does not work, long literals not supported

        String@A text = "Hello";

        int@A intPrim2 = Integer.valueOf(4);
        Integer@A integerObj2 = Integer.valueOf(3);

        boolean@B boolPrim2 = Boolean.valueOf(true);
        Boolean@B boolObj2 = Boolean.valueOf(false);

        long@C longPrim2 = Long.valueOf(3);
        Long@C longerObj2 = Long.valueOf(70);

        int@A a;
        int@A b;
        int@A c;
        int@A d;

        a = 1;
        b = 2;
        c = 3;
        d = 4;

        a = b = c = d;
	}
}
