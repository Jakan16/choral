import choral.channels.SymChannel;

public class ParenthesesExpressions@( A, B, C ){

    SymChannel@( A, B )< Object > ch_A_B;
    SymChannel@( B, C )< Object > ch_B_C;
    SymChannel@( C, A )< Object > ch_A_C;

    public ParenthesesExpressions(
        SymChannel@( A, B )< Object > ch_A_B,
        SymChannel@( B, C )< Object > ch_B_C,
        SymChannel@( C, A )< Object > ch_A_C
    ){
        this.ch_A_B = ch_A_B;
        this.ch_B_C = ch_B_C;
        this.ch_A_C = ch_A_C;
    }

    public int@A run(){
        int@A num = ((1@A + 1@B) + 1@C);
        return num;
    }

}