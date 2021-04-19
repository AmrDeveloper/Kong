package object;

import java.util.List;

public class KongArray extends KongObject{

    private final List<KongObject> elements;

    public KongArray(List<KongObject> elements) {
        super(ObjectType.ARRAY);
        this.elements = elements;
    }

    public List<KongObject> getElements() {
        return elements;
    }

    @Override
    public String inspect() {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for(int i = 0 ; i < elements.size() ; i++) {
            builder.append(elements.get(i).inspect());
            if(i != (elements.size() - 1)) {
                builder.append(", ");
            }
        }
        builder.append("]");
        return builder.toString();
    }
}
