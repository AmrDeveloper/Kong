package object;

public class KongMapPair {

    private final KongObject key;
    private final KongObject value;

    public KongMapPair(KongObject key, KongObject value) {
        this.key = key;
        this.value = value;
    }

    public KongObject getKey() {
        return key;
    }

    public KongObject getValue() {
        return value;
    }
}
