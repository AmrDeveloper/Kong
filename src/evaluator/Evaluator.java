package evaluator;

import ast.*;
import object.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Evaluator implements StatementVisitor<KongObject>, ExpressionVisitor<KongObject> {

    private static final KongNull NULL = new KongNull();
    private static final KongBoolean TRUE = new KongBoolean(true);
    private static final KongBoolean FALSE = new KongBoolean(false);

    private Environment environment;

    public Evaluator(Environment environment) {
        this.environment = environment;
    }

    @Override
    public KongObject visit(Program program) {
        KongObject result = null;
        for(Statement statement : program.getStatements()) {
            result = statement.accept(this);
            if (result instanceof KongReturn) return ((KongReturn) result).getValue();
            if (result instanceof KongError) return result;
        }
        return result;
    }

    @Override
    public KongObject visit(LetStatement statement) {
        KongObject value = statement.getValue().accept(this);
        if(isError(value)) return value;

        environment.set(statement.getName().getValue(), value);
        return null;
    }

    @Override
    public KongObject visit(ReturnStatement statement) {
        KongObject value = statement.getReturnValue().accept(this);
        if(isError(value)) return value;
        return new KongReturn(value);
    }

    @Override
    public KongObject visit(ExpressionStatement statement) {
        return statement.getExpression().accept(this);
    }

    @Override
    public KongObject visit(BlockStatement statement) {
        return evalStatements(statement.getStatements());
    }

    @Override
    public KongObject visit(Identifier expression) {
        KongObject value = environment.get(expression.getValue());
        if(value == null) {
            return newError("identifier not found: " + expression.getValue());
        }
        return value;
    }

    @Override
    public KongObject visit(IntegerLiteral expression) {
        return new KongInteger(expression.getValue());
    }

    @Override
    public KongObject visit(StringLiteral expression) {
        return new KongString(expression.getValue());
    }

    @Override
    public KongObject visit(BooleanLiteral expression) {
        return expression.isValue() ? TRUE : FALSE;
    }

    @Override
    public KongObject visit(PrefixExpression expression) {
        KongObject right = expression.getRight().accept(this);
        if(isError(right)) return right;
        return evalPrefixExpression(expression.getOperator(), right);
    }

    @Override
    public KongObject visit(InfixExpression expression) {
        KongObject left = expression.getLeft().accept(this);
        if(isError(left)) return left;

        KongObject right = expression.getRight().accept(this);
        if(isError(right)) return right;

        return evalInfixExpression(expression.getOperator(), left, right);
    }

    @Override
    public KongObject visit(IfExpression expression) {
        KongObject condition = expression.getCondition().accept(this);
        if(isError(condition)) return condition;

        if(isTruthy(condition)) {
            return expression.getConsequence().accept(this);
        } else if(expression.getAlternative() != null) {
            return expression.getAlternative().accept(this);
        }
        return NULL;
    }

    @Override
    public KongObject visit(FunctionLiteral expression) {
        List<Identifier> params = expression.getParameters();
        BlockStatement body = expression.getBody();
        return new Function(params, body, environment);
    }

    @Override
    public KongObject visit(CallExpression expression) {
        KongObject function = expression.getFunction().accept(this);
        if(isError(function)) return function;

        List<KongObject> args = evalExpressions(expression.getArguments(), environment);
        if(args.size() == 1 && isError(args.get(0))) return args.get(0);

        return applyFunction(function, args);
    }

    private KongObject evalStatements(List<Statement> statements) {
        KongObject result = null;
        for(Statement statement : statements) {
            result = statement.accept(this);
            if(result.getObjectType() == ObjectType.RETURN || result.getObjectType() == ObjectType.ERROR) {
                return result;
            }
        }
        return result;
    }

    private KongObject evalPrefixExpression(String operator, KongObject right) {
        switch (operator) {
            case "!": return evalBangOperatorExpression(right);
            case "-": return evalMinusPrefixOperatorExpression(right);
            default: return newError("unknown operator: %s%s", operator, right.getObjectType());
        }
    }

    private KongBoolean evalBangOperatorExpression(KongObject right) {
        if (right == TRUE) return FALSE;
        if (right == FALSE) return TRUE;
        if (right == NULL) return TRUE;
        return FALSE;
    }

    private KongObject evalMinusPrefixOperatorExpression(KongObject right) {
        if(right.getObjectType() != ObjectType.INTEGER) return newError("unknown operator: -%s", right.getObjectType());

        long value = ((KongInteger) right).getValue();
        return new KongInteger(-value);
    }

    private KongObject evalInfixExpression(String operator, KongObject left, KongObject right) {
        if(left.getObjectType() == ObjectType.INTEGER && right.getObjectType() == ObjectType.INTEGER) {
            return evalIntegerInfixExpression(operator, left, right);
        }
        if(left.getObjectType() == ObjectType.STRING && right.getObjectType() == ObjectType.STRING) {
            return evalStringInfixExpression(operator, left, right);
        }
        if(operator.equals("==")) return nativeBoolToBooleanObject(left == right);
        if(operator.equals("!=")) return nativeBoolToBooleanObject(left != right);
        if(left.getObjectType() != right.getObjectType()) {
            return newError("type mismatch: %s %s %s", left.getObjectType(), operator, right.getObjectType());
        }
        return newError("unknown operator: %s %s %s", left.getObjectType(), operator, right.getObjectType());
    }

    private KongObject evalIntegerInfixExpression(String operator, KongObject left, KongObject right) {
        long leftValue = ((KongInteger) left).getValue();
        long rightValue = ((KongInteger) right).getValue();
        switch (operator) {
            case "+":  return new KongInteger(leftValue + rightValue);
            case "-":  return new KongInteger(leftValue - rightValue);
            case "*":  return new KongInteger(leftValue * rightValue);
            case "/":  return new KongInteger(leftValue / rightValue);

            case "<":  return nativeBoolToBooleanObject(leftValue < rightValue);
            case ">":  return nativeBoolToBooleanObject(leftValue > rightValue);
            case "==": return nativeBoolToBooleanObject(leftValue == rightValue);
            case "!=": return nativeBoolToBooleanObject(leftValue != rightValue);
            default: return newError("unknown operator: %s %s %s", left.getObjectType(), operator, right.getObjectType());
        }
    }

    private KongObject evalStringInfixExpression(String operator, KongObject left, KongObject right) {
        String leftValue = ((KongString) left).getValue();
        String rightValue = ((KongString) right).getValue();
        switch (operator) {
            case "+":  return new KongString(leftValue + rightValue);
            case "<":  return nativeBoolToBooleanObject(leftValue.length() < rightValue.length());
            case ">":  return nativeBoolToBooleanObject(leftValue.length() > rightValue.length());
            case "==": return nativeBoolToBooleanObject(leftValue.equals(rightValue));
            case "!=": return nativeBoolToBooleanObject(!leftValue.equals(rightValue));
            default: return newError("unknown operator: %s %s %s", left.getObjectType(), operator, right.getObjectType());
        }
    }

    private KongObject applyFunction(KongObject function, List<KongObject> args) {
        if(!(function instanceof Function)) {
            return newError("not a function: %s", function.getObjectType());
        }

        Environment currentEnv = environment;

        Function functionObject = (Function) function;
        Environment extendedEnv = extendFunctionEnv(functionObject, args);

        environment = extendedEnv;
        KongObject evaluated = functionObject.getBody().accept(this);
        environment = currentEnv;

        return unwrapReturnValue(evaluated);
    }

    private Environment extendFunctionEnv(Function function, List<KongObject> args) {
        Environment env = new Environment(function.getEnvironment());
        List<Identifier> parameters = function.getParameters();
        for(int i = 0 ; i < parameters.size() ; i++) {
            env.set(parameters.get(i).getValue(), args.get(i));
        }
        return env;
    }

    private KongObject unwrapReturnValue(KongObject object) {
        if(object instanceof KongReturn) {
            return ((KongReturn) object).getValue();
        }
        return object;
    }

    private List<KongObject> evalExpressions(List<Expression> expressions, Environment env) {
        List<KongObject> result = new ArrayList<>();
        for(Expression expression : expressions) {
            KongObject evaluated = expression.accept(this);
            if (isError(evaluated)) {
                return Collections.singletonList(evaluated);
            }
            result.add(evaluated);
        }
        return result;
    }

    private KongBoolean nativeBoolToBooleanObject(boolean value) {
        return value ? TRUE : FALSE;
    }

    private boolean isTruthy(KongObject object) {
        if(object == NULL) return false;
        if(object == TRUE) return true;
        return object != FALSE;
    }

    private KongError newError(String format, Object... args) {
        return new KongError(String.format(format, args));
    }

    private boolean isError(KongObject object) {
        if(object != null) {
            return object.getObjectType() == ObjectType.ERROR;
        }
        return false;
    }
}
