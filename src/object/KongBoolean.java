package object;

public class KongBoolean extends KongObject implements Hashable {

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

    @Override
    public KongMapKey getHashKey() {
        int hash = value ? 1 : 0;
        return new KongMapKey(ObjectType.BOOLEAN, hash);
    }
}
