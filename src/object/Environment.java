package object;

import java.util.HashMap;
import java.util.Map;

public class Environment {

    private final Map<String, KongObject> store;
    private final Environment outerEnvironment;

    public Environment() {
        this.store = new HashMap<>();
        this.outerEnvironment = null;
    }

    public Environment(Environment outer) {
        this.store = new HashMap<>();
        this.outerEnvironment = outer;
    }

    public KongObject set(String name, KongObject value) {
        return store.put(name, value);
    }

    public KongObject get(String name) {
        KongObject object = store.get(name);
        if(object == null && outerEnvironment != null) {
            object = outerEnvironment.get(name);
        }
        return object;
    }
}
