package object;

public class KongString extends KongObject {

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
}
