package evaluator;

import ast.*;
import object.*;

import java.util.*;
import java.util.function.Function;

public class Evaluator implements StatementVisitor<KongObject>, ExpressionVisitor<KongObject> {

    private static final KongNull NULL = new KongNull();
    private static final KongBoolean TRUE = new KongBoolean(true);
    private static final KongBoolean FALSE = new KongBoolean(false);

    private final Map<String, BuiltinFunction> builtinFunctionsMap = new HashMap<>();

    private Environment environment;

    public Evaluator(Environment environment) {
        this.environment = environment;

        // Set Builtin Functions
        this.builtinFunctionsMap.put("len", new BuiltinFunction(builtinStringLength));
        this.builtinFunctionsMap.put("push", new BuiltinFunction(builtinArrayPush));
        this.builtinFunctionsMap.put("puts", new BuiltinFunction(builtinPrintln));
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
        if(value != null) return value;

        BuiltinFunction function = builtinFunctionsMap.get(expression.getValue());
        if(function != null) return function;

        return newError("identifier not found: " + expression.getValue());
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
        return new KongFunction(params, body, environment);
    }

    @Override
    public KongObject visit(CallExpression expression) {
        KongObject function = expression.getFunction().accept(this);
        if(isError(function)) return function;

        List<KongObject> args = evalExpressions(expression.getArguments());
        if(args.size() == 1 && isError(args.get(0))) return args.get(0);

        return applyFunction(function, args);
    }

    @Override
    public KongObject visit(ArrayLiteral expression) {
        List<KongObject> elements = evalExpressions(expression.getElements());
        if(elements.size() == 1 && isError(elements.get(0))) return elements.get(0);
        return new KongArray(elements);
    }

    @Override
    public KongObject visit(IndexExpression expression) {
        KongObject left = expression.getLeft().accept(this);
        if(isError(left)) return left;

        KongObject index = expression.getIndex().accept(this);
        if(isError(index)) return index;

        return evalIndexExpression(left, index);
    }

    @Override
    public KongObject visit(MapLiteral expression) {
        Map<Expression, Expression> expressionsPairs = expression.getPairs();
        Map<KongMapKey, KongMapPair> kongObjectsMap = new HashMap<>();
        for(Map.Entry<Expression, Expression> expressionPair : expressionsPairs.entrySet()) {
            KongObject key = expressionPair.getKey().accept(this);
            if(isError(key)) return key;
            if(!(key instanceof Hashable)) {
                return newError("unusable as hash key: %s", key.getObjectType());
            }

            KongObject value = expressionPair.getValue().accept(this);
            if(isError(value)) return value;

            Hashable hashable = (Hashable) key;
            KongMapPair pair = new KongMapPair(key, value);
            kongObjectsMap.put(hashable.getHashKey(), pair);
        }
        return new KongMap(kongObjectsMap);
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

    private KongObject evalIndexExpression(KongObject left, KongObject index) {
        if(left.getObjectType() == ObjectType.ARRAY && index.getObjectType() == ObjectType.INTEGER) {
            return evalArrayIndexExpression(left, index);
        }
        if(left.getObjectType() == ObjectType.MAP) {
            return evalMapIndexExpression(left, index);
        }
        if(left.getObjectType() == ObjectType.STRING && index.getObjectType() == ObjectType.INTEGER) {
            return evalStringIndexExpression(left, index);
        }
        return newError("index operator not supported: %s", left.getObjectType());
    }

    private KongObject evalArrayIndexExpression(KongObject array, KongObject index) {
        KongArray kongArray = (KongArray) array;
        long kongIndex = ((KongInteger) index).getValue();
        int maxLength = kongArray.getElements().size() - 1;

        if(kongIndex < 0 || kongIndex > maxLength) return NULL;
        return kongArray.getElements().get((int) kongIndex);
    }

    private KongObject evalMapIndexExpression(KongObject map, KongObject key) {
        if(!(key instanceof Hashable)) {
            return newError("unusable as hash key: %s", key.getObjectType());
        }
        Hashable hashable = (Hashable) key;
        KongMap kongMap = (KongMap) map;
        KongMapPair pair = kongMap.getPairs().get(hashable.getHashKey());
        if(pair == null) return NULL;
        return pair.getValue();
    }

    private KongObject evalStringIndexExpression(KongObject string, KongObject index) {
        KongString kongString = (KongString) string;
        long kongIndex = ((KongInteger) index).getValue();
        int maxLength = kongString.getValue().length() - 1;

        if(kongIndex < 0 || kongIndex > maxLength) return NULL;
        String charAtPosition = String.valueOf(kongString.getValue().charAt((int) kongIndex));
        return new KongString(charAtPosition);
    }

    private KongObject applyFunction(KongObject function, List<KongObject> args) {
        if(function instanceof KongFunction) {
            Environment currentEnv = environment;

            KongFunction functionObject = (KongFunction) function;
            Environment extendedEnv = extendFunctionEnv(functionObject, args);

            environment = extendedEnv;
            KongObject evaluated = functionObject.getBody().accept(this);
            environment = currentEnv;

            return unwrapReturnValue(evaluated);
        }

        else if(function instanceof BuiltinFunction) {
            BuiltinFunction builtinFunction = (BuiltinFunction) function;
            return builtinFunction.getFunction().apply(args);
        }

        return newError("not a function: %s", function.getObjectType());
    }

    private Environment extendFunctionEnv(KongFunction function, List<KongObject> args) {
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

    private List<KongObject> evalExpressions(List<Expression> expressions) {
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

    private final Function<List<KongObject>, KongObject> builtinStringLength = args -> {
        if(args.size() != 1) {
            return newError("wrong number of arguments. got=%d, want=1", args.size());
        }

        switch (args.get(0).getObjectType()) {
            case STRING: {
                String value = ((KongString )args.get(0)).getValue();
                return new KongInteger(value.length());
            }
            case ARRAY: {
                KongArray value = ((KongArray)args.get(0));
                int length = value.getElements().size();
                return new KongInteger(length);
            }
            default: {
                return newError("argument to `len` not supported, got %s", args.get(0).getObjectType());
            }
        }
    };

    private final Function<List<KongObject>, KongObject> builtinArrayPush = args -> {
        if(args.size() != 2) {
            return newError("wrong number of arguments. got=%d, want=2", args.size());
        }

        if(args.get(0).getObjectType() != ObjectType.ARRAY) {
            return newError("argument to `push` must be ARRAY, got %s", args.get(0).getObjectType());
        }

        KongArray array = (KongArray) args.get(0);
        List<KongObject> elements = array.getElements();
        elements.add(args.get(1));
        return array;
    };

    private final Function<List<KongObject>, KongObject> builtinPrintln = args -> {
        for(KongObject arg : args) {
            System.out.println(arg.inspect());
        }
        return NULL;
    };
}
