package object;

import java.util.Map;

public class KongMap extends KongObject{

    private final Map<KongMapKey, KongMapPair> pairs;

    public KongMap(Map<KongMapKey, KongMapPair> pairs) {
        super(ObjectType.MAP);
        this.pairs = pairs;
    }

    public Map<KongMapKey, KongMapPair> getPairs() {
        return pairs;
    }

    @Override
    public String inspect() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        int counter = 0;
        int len = pairs.size() - 1;
        for(Map.Entry<KongMapKey, KongMapPair> pair : pairs.entrySet()) {
            builder.append(pair.getValue().getKey().inspect());
            builder.append(":");
            builder.append(pair.getValue().getValue().inspect());
            if(counter++ < len) builder.append(", ");
        }
        builder.append("}");
        return builder.toString();
    }
}
