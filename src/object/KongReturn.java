package object;

public class KongReturn extends KongObject{

    private final KongObject value;

    public KongReturn(KongObject value) {
        super(ObjectType.RETURN);
        this.value = value;
    }

    public KongObject getValue() {
        return value;
    }

    @Override
    public String inspect() {
        return value.inspect();
    }
}
