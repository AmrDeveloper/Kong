package object;

public class KongString extends KongObject implements Hashable{

    private final String value;

    public KongString(String value) {
        super(ObjectType.STRING);
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String inspect() {
        return value;
    }

    @Override
    public KongMapKey getHashKey() {
        return new KongMapKey(ObjectType.STRING, value.hashCode());
    }
}
