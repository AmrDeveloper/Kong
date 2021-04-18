package object;

public abstract class KongObject {

    private final ObjectType objectType;

    public KongObject(ObjectType type) {
        this.objectType = type;
    }

    public ObjectType getObjectType() {
        return objectType;
    }

    abstract public String inspect();
}
