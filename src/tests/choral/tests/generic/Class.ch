import java.util.*;

public class Class@( A, B ) < K@Y, T@X extends Y@X, Y@X extends List@X< String > >{

    // field with generic type
    K@B value;

    // generic parameter in constructor
    Class( K@B value ){
        this.value = value;
    }

    String@A func( T@A list ){
        // return type of method depends on type argument
        return list.get( 0 ).trim();
    }

    // generic parameter in function
    int@A hash( K@A object ){
        // generics implements Object methods
        return object.hashCode();
    }

    // return generic
    K@B getVal(){
        return value;
    }
}