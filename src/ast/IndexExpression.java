package ast;

public class IndexExpression extends Expression {

    private final Expression left;
    private final Expression index;

    public IndexExpression(Expression left, Expression index) {
        this.left = left;
        this.index = index;
    }

    public Expression getLeft() {
        return left;
    }

    public Expression getIndex() {
        return index;
    }

    @Override
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append('(');
        builder.append(left.toString());
        builder.append("[");
        builder.append(index.toString());
        builder.append("])");
        return builder.toString();
    }
}
