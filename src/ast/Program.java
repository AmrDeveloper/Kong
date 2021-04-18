package ast;

import java.util.List;

public class Program extends Statement{

    private final List<Statement> statements;

    public Program(List<Statement> statements) {
        this.statements = statements;
    }

    public List<Statement> getStatements() {
        return statements;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for(Statement statement : statements) {
            builder.append(statement.toString());
        }
        return builder.toString();
    }

    @Override
    public <R> R accept(StatementVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
