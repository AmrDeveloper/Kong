package object;

import java.util.Objects;

public class KongMapKey {

    private final ObjectType type;
    private final int value;

    public KongMapKey(ObjectType type, int value) {
        this.type = type;
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public ObjectType getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KongMapKey that = (KongMapKey) o;
        return value == that.value && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, value);
    }
}
