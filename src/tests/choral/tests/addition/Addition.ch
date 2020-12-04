import choral.channels.SymChannel;

public class Addition@( A, B, C ) {

    SymChannel@( A, B )< Object > ch_A_B;
    SymChannel@( A, C )< Object > ch_A_C;
    SymChannel@( B, C )< Object > ch_B_C;

    public Addition(SymChannel@( A, B )< Object > ch_A_B, SymChannel@( A, C )< Object > ch_A_C, SymChannel@( B, C )< Object > ch_B_C){
        this.ch_A_B = ch_A_B;
        this.ch_A_C = ch_A_C;
        this.ch_B_C = ch_B_C;
    }

	public int@A add() {
		return 5@A + 10@B + 15@C;
	}
}
