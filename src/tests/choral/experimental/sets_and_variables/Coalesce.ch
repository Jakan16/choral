import choral.channels.SymChannel;

public class Coalesce@( A, B, C ){

     private final SymChannel@( A, B )< Object > ch_A_B;
     private final SymChannel@( A, C )< Object > ch_A_C;
     private final SymChannel@( B, C )< Object > ch_B_C;

     public Coalesce( SymChannel@( A, B )< Object > ch_A_B, SymChannel@( A, C )< Object > ch_A_C, SymChannel@( B, C )< Object > ch_B_C ){
        this.ch_A_B = ch_A_B;
        this.ch_A_C = ch_A_C;
        this.ch_B_C = ch_B_C;

        int num = 1@B + 2@A;
        int@A num2 = num + 3@C;
        int@C num3 = num + 3@C;

        int val = 2@B + 1@A;
        int@A val2 = val + 3@A + 4@B;

        int _val = 2@A + 1@B;
        int@A _val2 = _val + 3@B + 4@A;
     }
}