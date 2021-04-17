package ast;

public class PrefixExpression extends Expression{

    private final String operator;
    private final Expression right;

    public PrefixExpression(String operator, Expression right) {
        this.operator = operator;
        this.right = right;
    }

    public String getOperator() {
        return operator;
    }

    public Expression getRight() {
        return right;
    }

    @Override
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("(");
        builder.append(operator);
        builder.append(right.toString());
        builder.append(")");
        return builder.toString();
    }
}
