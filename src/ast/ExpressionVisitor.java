package ast;

public interface ExpressionVisitor<R> {
    R visit(Identifier expression);
    R visit(IntegerLiteral expression);
    R visit(StringLiteral expression);
    R visit(BooleanLiteral expression);
    R visit(PrefixExpression expression);
    R visit(InfixExpression expression);
    R visit(IfExpression expression);
    R visit(FunctionLiteral expression);
    R visit(CallExpression expression);
    R visit(ArrayLiteral expression);
    R visit(IndexExpression expression);
}
