package ast;

import java.util.List;

public class CallExpression extends Expression {

    // Identifier or FunctionLiteral
    private final Expression function;
    private final List<Expression> arguments;

    public CallExpression(Expression function, List<Expression> arguments) {
        this.function = function;
        this.arguments = arguments;
    }

    public Expression getFunction() {
        return function;
    }

    public List<Expression> getArguments() {
        return arguments;
    }

    @Override
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(function.toString());
        builder.append("(");
        for(int i = 0 ; i < arguments.size() ; i++) {
            builder.append(arguments.get(i).toString());
            if(i != (arguments.size() - 1)) {
                builder.append(", ");
            }
        }
        builder.append(")");
        return builder.toString();
    }
}
