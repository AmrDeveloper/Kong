package object;

public class KongNull extends KongObject{

    public KongNull() {
        super(ObjectType.NULL);
    }

    @Override
    public String inspect() {
        return "null";
    }
}
