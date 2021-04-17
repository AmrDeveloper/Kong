package ast;

public class InfixExpression extends Expression {

    private final Expression left;
    private final String operator;
    private final Expression right;

    public InfixExpression(Expression left, String operator, Expression right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    public Expression getLeft() {
        return left;
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
        builder.append(left.toString());
        builder.append(" " + operator + " ");
        builder.append(right.toString());
        builder.append(")");
        return builder.toString();
    }
}
