package object;


import ast.BlockStatement;
import ast.Identifier;

import java.util.List;

public class KongFunction extends KongObject {

    private final List<Identifier> parameters;
    private final BlockStatement body;
    private final Environment environment;

    public KongFunction(List<Identifier> parameters, BlockStatement body, Environment environment) {
        super(ObjectType.FUNCTION);
        this.parameters = parameters;
        this.body = body;
        this.environment = environment;
    }

    public List<Identifier> getParameters() {
        return parameters;
    }

    public BlockStatement getBody() {
        return body;
    }

    public Environment getEnvironment() {
        return environment;
    }

    @Override
    public String inspect() {
        StringBuilder builder = new StringBuilder();
        builder.append("fn");
        builder.append("(");
        for(int i = 0 ; i < parameters.size() ; i++) {
            builder.append(parameters.get(i).toString());
            if(i != (parameters.size() - 1)) {
                builder.append(", ");
            }
        }
        builder.append(") {\n");
        builder.append(body.toString());
        builder.append("}");
        return builder.toString();
    }
}
