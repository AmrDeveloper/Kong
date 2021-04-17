package ast;

public class ReturnStatement extends Statement{

    private final Expression returnValue;

    public ReturnStatement(Expression returnValue) {
        this.returnValue = returnValue;
    }

    public Expression getReturnValue() {
        return returnValue;
    }

    @Override
    public <R> R accept(StatementVisitor<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("return ");

        if(returnValue != null) {
            builder.append(returnValue.toString());
        }

        builder.append(";");
        return builder.toString();
    }
}
