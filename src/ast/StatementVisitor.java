package ast;

public interface StatementVisitor<R> {
    R visit(Program statement);
    R visit(LetStatement statement);
    R visit(ReturnStatement statement);
    R visit(ExpressionStatement statement);
    R visit(BlockStatement statement);
}
