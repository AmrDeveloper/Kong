package ast;

public abstract class Statement {
    public abstract <R> R accept(StatementVisitor<R> visitor);
}
