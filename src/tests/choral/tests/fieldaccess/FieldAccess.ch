
public class FieldAccess@( A ){

    private int@A number;

    public int@A sum(){
        number = 5;
        StaticClass.number = 7;
        return number + this.number + new OtherClass().number + StaticClass.number;
    }

}