package choral.nestedif;

import org.choral.channels.SymChannel;

public class OtherClass@( D, F ) {

  public void print(SymChannel@( D, F )< Object > ch_D_F, boolean@D con, String@F str){
    if(con){
      System@F.out.println(str);
    }
  }

  public static void staticPrint(SymChannel@( D, F )< Object > ch_D_F, boolean@D con, String@F str){
    if(con){
      System@F.out.println(str);
    }
  }

  public static void staticNoParam(){}

}
