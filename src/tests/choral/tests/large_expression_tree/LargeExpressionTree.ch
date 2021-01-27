import choral.channels.SymChannel;

public class LargeExpressionTree@( A, B, C, D ){
	SymChannel@( A, B )< Object > ch_A_B;
	SymChannel@( B, C )< Object > ch_B_C;
	SymChannel@( C, A )< Object > ch_A_C;
	SymChannel@( A, D )< Object > ch_A_D;
    SymChannel@( B, D )< Object > ch_B_D;
    SymChannel@( C, D )< Object > ch_C_D;

	public LargeExpressionTree(
		SymChannel@( A, B )< Object > ch_A_B,
		SymChannel@( B, C )< Object > ch_B_C,
		SymChannel@( C, A )< Object > ch_A_C,
		SymChannel@( A, D )< Object > ch_A_D,
        SymChannel@( B, D )< Object > ch_B_D,
        SymChannel@( C, D )< Object > ch_C_D
	){
		this.ch_A_B = ch_A_B;
		this.ch_B_C = ch_B_C;
		this.ch_A_C = ch_A_C;
		this.ch_A_D = ch_A_D;
        this.ch_B_D = ch_B_D;
        this.ch_C_D = ch_C_D;
	}

	public int@A calc(){
	    int@A num = (3@B + 4@D) + ((0@C + (1@D + 2@B)) + ((6@C + ((8@B + 9@C) + 7@C)) + 5@D));

	    int@A num2 = (3@B + 4@D) + ((0@C + (1@D + 2@B)) + ((6@C + ((8@A + 9@C) + 7@C)) + 5@D));

	    int@A num4 = 1@A + 1;
	    num4 = 1 + 1;
	    num4 = 1 + 1@C + 1@B;
	    num4 = 1 + 1 + 1;
	    num4 = 1@A + 1 + 2@B;
	    num4 = 1@A + 1 + 2@B + 2;
	    num4 = (1@A + 1) + (2@B + 2);
	    return num;
	}

}