package object;

import java.util.List;
import java.util.function.Function;

public class BuiltinFunction extends KongObject{

    private final Function<List<KongObject>, KongObject> function;

    public BuiltinFunction(Function<List<KongObject>, KongObject> function) {
        super(ObjectType.BUILTIN);
        this.function = function;
    }

    public Function<List<KongObject>, KongObject> getFunction() {
        return function;
    }

    @Override
    public String inspect() {
        return "builtin function";
    }
}
