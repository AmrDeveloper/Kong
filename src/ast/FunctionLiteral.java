package ast;

import java.util.List;

public class FunctionLiteral extends Expression {

    private final List<Identifier> parameters;
    private final BlockStatement body;

    public FunctionLiteral(List<Identifier> parameters, BlockStatement body) {
        this.parameters = parameters;
        this.body = body;
    }

    public List<Identifier> getParameters() {
        return parameters;
    }

    public BlockStatement getBody() {
        return body;
    }

    @Override
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("fn");
        builder.append("(");
        for(int i = 0 ; i < parameters.size() ; i++) {
            builder.append(parameters.get(i).toString());
            if(i != (parameters.size() - 1)) {
                builder.append(", ");
            }
        }
        builder.append(") ");
        builder.append(body.toString());
        return builder.toString();
    }
}
