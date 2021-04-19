package ast;

import java.util.List;

public class ArrayLiteral extends Expression {

    private final List<Expression> elements;

    public ArrayLiteral(List<Expression> elements) {
        this.elements = elements;
    }

    public List<Expression> getElements() {
        return elements;
    }

    @Override
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for(int i = 0 ; i < elements.size() ; i++) {
            builder.append(elements.get(i).toString());
            if(i != (elements.size() - 1)) {
                builder.append(", ");
            }
        }
        builder.append("]");
        return builder.toString();
    }
}
