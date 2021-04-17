package ast;

public class LetStatement extends Statement {

    private final Identifier name;
    private final Expression value;

    public LetStatement(Identifier name, Expression value) {
        this.name = name;
        this.value = value;
    }

    public Identifier getIdentifier() {
        return name;
    }

    public Expression getValue() {
        return value;
    }

    @Override
    public <R> R accept(StatementVisitor<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("let ");
        builder.append(name.getValue());
        builder.append(" = ");

        if(value != null) {
            builder.append(value.toString());
        }

        builder.append(";");

        return builder.toString();
    }
}
