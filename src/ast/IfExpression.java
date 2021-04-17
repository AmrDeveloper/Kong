package ast;

public class IfExpression extends Expression {

    private final Expression condition;
    private final BlockStatement consequence;
    private final BlockStatement alternative;

    public IfExpression(Expression condition, BlockStatement consequence, BlockStatement alternative) {
        this.condition = condition;
        this.consequence = consequence;
        this.alternative = alternative;
    }

    public Expression getCondition() {
        return condition;
    }

    public BlockStatement getConsequence() {
        return consequence;
    }

    public BlockStatement getAlternative() {
        return alternative;
    }

    @Override
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("if");
        builder.append(condition.toString());
        builder.append(" ");
        builder.append(consequence.toString());

        if(alternative != null) {
            builder.append("else ");
            builder.append(alternative.toString());
        }

        return builder.toString();
    }
}
