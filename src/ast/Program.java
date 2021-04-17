package ast;

import java.util.List;

public class Program {

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
}
