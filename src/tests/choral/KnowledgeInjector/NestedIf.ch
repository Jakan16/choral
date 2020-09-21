package choral.nestedif;

import org.choral.channels.SymChannel;

public class NestedIf@( A, B, C ) {
	public static void nested( SymChannel@( A, B )< Object > ch_A_B, SymChannel@( A, C )< Object > ch_A_C, SymChannel@( B, C )< Object > ch_B_C ){

    boolean@A bool = true@A;
    boolean@B bool2 = false@B;
    if (bool2){
      System@A.out.println("Hey"@A);
      if (bool2){
        System@B.out.println("B"@B);
      }else{
        System@A.out.println("A"@A);
      }
    }else{
      System@A.out.println("Hey"@A);
      if (bool2){
        System@B.out.println("B2"@B);
      }else{
        System@A.out.println("A2"@A);
      }
    }

    if(true@A){
      if(true@B){
        System@C.out.println("C"@C);
      }
    }

    if(true@A){
      if(2@B > 3@B){
        System@C.out.println("C"@C);
      }
    }


    if(true@A){
      if(true@B){
        System@C.out.println("C"@C);
      }else{
        System@C.out.println("C"@C);
      }
    }else{
      if(true@A){
        System@C.out.println("C"@C);
      }else{
        System@C.out.println("C"@C);
      }
    }

		if(true@A){
			if(true@A){
				System@C.out.println("C"@C);
			}else{
				System@C.out.println("C"@C);
			}
		}else{
			{System@C.out.println("C"@C);}
		}

		if(true@A){
			System@A.out.println("A"@A);
		}

		if(true@A){
			System@C.out.println("C"@C);
		}

        if(true@A){
          if(true@B){
            System@C.out.println("C"@C);
          }
        }else{
          System@B.out.println("B"@B);
          if(true@B){
            System@C.out.println("C"@C);
          }
        }

	    if(true@A){
          if(true@B){
            System@C.out.println("C"@C);
          }
        }else{
          System@B.out.println("B"@B);
          if(true@B){
            System@C.out.println("C"@C);
          }
        }

		if(true@A){
			String@B b;
		}else{
			String@B b2;
		}

		if(true@A){
			String@B b = "B"@B;
		}else{
			String@B b = "B2"@B;
		}

		String@B b = "b"@B;
		if(true@A){
			testFunc("A"@A, b);
		}

		if(true@C){
			OtherClass@(A,B) oc = new OtherClass@(A,B)();
			oc.print(ch_A_B, true@A, "Hello"@B);
		}

		if(true@C){
			OtherClass@(A,B).staticPrint(ch_A_B, true@A, "Hello"@B);
		}

		if(true@A){
			OtherClass@(A,B).staticNoParam();
		}

		if(true@B){
			if(pred()){
				System@A.out.println("A"@A);
			}
		}

        if(true@A){
            new OtherClass@(A,B)();
        }

  }

	public static void testFunc(String@A a, String@B b){}

	public static boolean@A pred(){
		return true@A;
	}
}
