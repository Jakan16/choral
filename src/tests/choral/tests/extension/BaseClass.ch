import java.util.*;

public class BaseClass@( A, B ) < T@X extends String@X >{

    // field with generic type
    T@B value;

    // generic parameter in constructor
    //BaseClass( T@B value ){
    //   this.value = value;
    //}

    // return generic
    public T@B getVal(){
        return value;
    }

    void setVal( T@B value ){
        this.value = value;
    }

    // Something to overwrite
    public List@B< T > getValTwice(){
        ArrayList< T > list = new ArrayList< T >();
        list.add( value );
        list.add( value );

        return list;
    }
}