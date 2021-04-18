package object;

public class KongBoolean extends KongObject{

    private final boolean value;

    public KongBoolean(boolean value) {
        super(ObjectType.BOOLEAN);
        this.value = value;
    }

    @Override
    public ObjectType getObjectType() {
        return super.getObjectType();
    }

    @Override
    public String inspect() {
        return String.valueOf(value);
    }
}
