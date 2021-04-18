package ast;

public abstract class Statement extends Node {
    public abstract <R> R accept(StatementVisitor<R> visitor);
}
