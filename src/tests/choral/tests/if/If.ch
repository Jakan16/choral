import choral.channels.SymChannel;

public class If@( A, B, C ) {

    SymChannel@( A, B )< Object > ch_A_B;
    SymChannel@( A, C )< Object > ch_A_C;
    SymChannel@( B, C )< Object > ch_B_C;

    public If(SymChannel@( A, B )< Object > ch_A_B, SymChannel@( A, C )< Object > ch_A_C, SymChannel@( B, C )< Object > ch_B_C){
        this.ch_A_B = ch_A_B;
        this.ch_A_C = ch_A_C;
        this.ch_B_C = ch_B_C;
    }

	public void test() {
		if(true){}

		if(2@A > 3@B){
            System@C.out.println(2@A);
        }else{
            System@C.out.println(3@A);
        }

		if(2@A > 3@B){
		    System@C.out.println(2@A);
		}else{
		    System@C.out.println(3@B);
		}

		if(true){
		    if(true){
		        if(true){
		            if(true){
		                System.out.println("nested");
                	}
            	}
        	}
		}

		if(true){
		    System@A.out.println("1");
		    System@B.out.println("2");
		}else{
		    System@B.out.println("2");
            System@A.out.println("1");
		}

		if(true){
            if(true){
                System@A.out.println("4");
                System@B.out.println("5");
            }else{
                System@B.out.println("4");
                System@A.out.println("5");
            }
        }else{
            if(true){
                System@A.out.println("4");
                System@B.out.println("5");
            }else{
                System@B.out.println("4");
                System@A.out.println("5");
            }
        }
	}
}
