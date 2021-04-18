package ast;

public abstract class Expression extends Node{
    public abstract <R> R accept(ExpressionVisitor<R> visitor);
}
