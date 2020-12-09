import choral.channels.SymChannel;

public class AutoBoxing@( A, B ) {

        SymChannel@( A, B )< Object > ch_A_B;

        public AutoBoxing( SymChannel@( A, B )< Object > ch_A_B ){
            this.ch_A_B = ch_A_B;
        }

        public void f(){
            int@A numberPrimitive;
            Integer@A numberObject;

            // no (un)boxing necessary
            numberPrimitive = f0( 2@A );
            numberObject    = f1( Integer.valueOf( 2 ) );

            // (un)boxing in assignments
            numberPrimitive  = Integer@A.valueOf( 2@A );
            numberObject = 2@A;

            // unboxing in invocations
            f2( Integer@A.valueOf( 2@A ) );

            // boxing in overloaded invocation
            f3( 2@B );
            f4( Integer@A.valueOf( 2@A ) );
        }

        public int@A     f0( int@A     i ) { return i; }
        private String@A f1( String@A i, Integer@A k ) { return i; }
        private Integer@A f1( Integer@A i ) { return i; }

        // Does not work, no unique signature at B
        //public int@A     f1( int@A     i ) { return i; }
        //public Integer@A f1( Integer@A i ) { return i; }

        public void f2( int@A i ) {
            //return; Does not work
        }

        public void f3( Integer@A i ) { }
        public void f3( Integer@B i ) { }

        public void f4( int@A i ) { }
        public void f4( int@B i ) { }
}