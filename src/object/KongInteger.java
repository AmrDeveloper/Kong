package object;

public class KongInteger extends KongObject implements Hashable{

    private final long value;

    public KongInteger(long value) {
        super(ObjectType.INTEGER);
        this.value = value;
    }

    public long getValue() {
        return value;
    }

    @Override
    public String inspect() {
        return String.valueOf(value);
    }

    @Override
    public KongMapKey getHashKey() {
        return new KongMapKey(ObjectType.INTEGER, (int) value);
    }
}
