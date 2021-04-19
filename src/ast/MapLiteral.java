package ast;

import java.util.Map;

public class MapLiteral extends Expression {

    private final Map<Expression, Expression> pairs;

    public MapLiteral(Map<Expression, Expression> pairs) {
        this.pairs = pairs;
    }

    public Map<Expression, Expression> getPairs() {
        return pairs;
    }

    @Override
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        int counter = 0;
        int len = pairs.size() - 1;
        for(Map.Entry<Expression, Expression> pair : pairs.entrySet()) {
            builder.append(pair.getKey().toString());
            builder.append(":");
            builder.append(pair.getValue().toString());
            if(counter++ < len) builder.append(", ");
        }
        builder.append("}");
        return builder.toString();
    }
}
