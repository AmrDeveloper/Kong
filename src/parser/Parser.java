package parser;

import ast.*;
import lexer.Lexer;
import token.Token;
import token.TokenType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static ast.Precedence.*;

public class Parser {

    private final Lexer lexer;
    private Token currentToken;
    private Token peekToken;
    private final List<String> errors;
    private final Map<TokenType, Supplier<Expression>> prefixParseFns;
    private final Map<TokenType, Function<Expression, Expression>> infixParseFns;

    private static final Map<TokenType, Precedence> precedences = new HashMap<>();

    static {
        precedences.put(TokenType.EQ, EQUALS);
        precedences.put(TokenType.NOT_EQ, EQUALS);
        precedences.put(TokenType.LT, LESSGREATER);
        precedences.put(TokenType.GT, LESSGREATER);
        precedences.put(TokenType.PLUS, SUM);
        precedences.put(TokenType.MINUS, SUM);
        precedences.put(TokenType.SLASH, PRODUCT);
        precedences.put(TokenType.ASTERISK, PRODUCT);
        precedences.put(TokenType.L_PAREN, CALL);
        precedences.put(TokenType.L_BRACKET, INDEX);
    }

    public Parser(Lexer lexer) {
        this.lexer = lexer;
        this.errors = new ArrayList<>();

        this.prefixParseFns = new HashMap<>();
        registerPrefix(TokenType.IDENT, parseIdentifier);
        registerPrefix(TokenType.INT, parseIntegerLiteral);
        registerPrefix(TokenType.STRING, parseStringLiteral);
        registerPrefix(TokenType.BANG, parsePrefixExpression);
        registerPrefix(TokenType.MINUS, parsePrefixExpression);
        registerPrefix(TokenType.TRUE, parseBooleanExpression);
        registerPrefix(TokenType.FALSE, parseBooleanExpression);
        registerPrefix(TokenType.L_PAREN, parseGroupedExpression);
        registerPrefix(TokenType.IF, parseIfExpression);
        registerPrefix(TokenType.FUNCTION, parseFunctionLiteral);
        registerPrefix(TokenType.L_BRACKET, parseArrayLiteral);
        registerPrefix(TokenType.L_BRACE, parseMapLiteral);

        this.infixParseFns = new HashMap<>();
        registerInfix(TokenType.PLUS, parseInfixExpression);
        registerInfix(TokenType.MINUS, parseInfixExpression);
        registerInfix(TokenType.SLASH, parseInfixExpression);
        registerInfix(TokenType.ASTERISK, parseInfixExpression);
        registerInfix(TokenType.EQ, parseInfixExpression);
        registerInfix(TokenType.NOT_EQ, parseInfixExpression);
        registerInfix(TokenType.LT, parseInfixExpression);
        registerInfix(TokenType.GT, parseInfixExpression);
        registerInfix(TokenType.L_PAREN, parseCallExpression);
        registerInfix(TokenType.L_BRACKET, parseIndexExpression);

        // Read two tokens, so curToken and peekToken are both set
        nextToken();
        nextToken();
    }

    private void registerPrefix(TokenType tokenType, Supplier<Expression> prefixParseFn) {
        prefixParseFns.put(tokenType, prefixParseFn);
    }

    private void registerInfix(TokenType tokenType, Function<Expression, Expression> infixParseFn) {
        infixParseFns.put(tokenType, infixParseFn);
    }

    public Program parseProgram() {
        List<Statement> statements = new ArrayList<>();
        while (currentToken.getTokenType() != TokenType.EOF) {
            Statement statement = parseStatement();
            if(statement != null) {
                statements.add(statement);
            }
            nextToken();
        }
        return new Program(statements);
    }

    private Statement parseStatement() {
        switch (currentToken.getTokenType()) {
            case LET: return parseLetStatement();
            case RETURN: return parseReturnStatement();
            default: return parseExpressionStatement();
        }
    }

    private LetStatement parseLetStatement() {
        if(!expectPeek(TokenType.IDENT)) {
            return null;
        }

        Identifier identifier = new Identifier(currentToken.getLiteral());
        if(!expectPeek(TokenType.ASSIGN)) {
            return null;
        }

        nextToken();

        Expression value = parseExpression(LOWEST);

        // encounter a semicolon
        if (!currentTokenIs(TokenType.SEMICOLON)){
            nextToken();
        }

        return new LetStatement(identifier, value);
    }

    private ReturnStatement parseReturnStatement() {
        nextToken();

        Expression returnValue = parseExpression(LOWEST);

        // encounter a semicolon
        if (!currentTokenIs(TokenType.SEMICOLON)) nextToken();

        return new ReturnStatement(returnValue);
    }

    private ExpressionStatement parseExpressionStatement() {
        Expression expression = parseExpression(LOWEST);

        if(peekTokenIs(TokenType.SEMICOLON)) {
            nextToken();
        }

        return new ExpressionStatement(expression);
    }

    private BlockStatement parseBlockStatement() {
        List<Statement> statements = new ArrayList<>();

        nextToken();

        while (!currentTokenIs(TokenType.R_BRACE) && !currentTokenIs(TokenType.EOF)) {
            Statement statement = parseStatement();
            if(statement != null) statements.add(statement);
            nextToken();
        }

        return new BlockStatement(statements);
    }

    private Expression parseExpression(Precedence precedence) {
        Supplier<Expression> prefix = prefixParseFns.get(currentToken.getTokenType());
        if(prefix == null) {
            noPrefixParseFnError(currentToken.getTokenType());
            return null;
        }

        Expression leftExp = prefix.get();

        while (!peekTokenIs(TokenType.SEMICOLON) && precedence.getRank() < peekPrecedence().getRank()) {
            Function<Expression, Expression> infix = infixParseFns.get(peekToken.getTokenType());
            if(infix == null) return leftExp;
            nextToken();
            leftExp = infix.apply(leftExp);
        }

        return leftExp;
    }

    private List<Expression> parseExpressionList(TokenType endToken) {
        List<Expression> expressions = new ArrayList<>();

        if(peekTokenIs(endToken)) {
            nextToken();
            return expressions;
        }

        nextToken();
        expressions.add(parseExpression(LOWEST));

        while (peekTokenIs(TokenType.COMMA)) {
            nextToken();
            nextToken();
            expressions.add(parseExpression(LOWEST));
        }

        if(!expectPeek(endToken)) return null;

        return expressions;
    }

    private List<Identifier> parseFunctionParameters() {
        List<Identifier> identifiers = new ArrayList<>();

        if(peekTokenIs(TokenType.R_PAREN)) {
            nextToken();
            return identifiers;
        }

        nextToken();

        Identifier identifier = new Identifier(currentToken.getLiteral());
        identifiers.add(identifier);

        while (peekTokenIs(TokenType.COMMA)) {
            nextToken();
            nextToken();
            Identifier newIdentifier = new Identifier(currentToken.getLiteral());
            identifiers.add(newIdentifier);
        }

        if(!expectPeek(TokenType.R_PAREN)) return null;

        return identifiers;
    }

    private final Supplier<Expression> parseIdentifier = () -> new Identifier(currentToken.getLiteral());

    private final Supplier<Expression> parseIntegerLiteral = () -> {
        long value = Long.parseLong(currentToken.getLiteral());
        return new IntegerLiteral(value);
    };

    private final Supplier<Expression> parseStringLiteral = () -> {
        return new StringLiteral(currentToken.getLiteral());
    };

    private final Supplier<Expression> parsePrefixExpression = () -> {
        String operator = currentToken.getLiteral();
        nextToken();
        Expression right = parseExpression(PREFIX);
        return new PrefixExpression(operator, right);
    };

    private final Supplier<Expression> parseBooleanExpression = () -> new BooleanLiteral(currentTokenIs(TokenType.TRUE));

    private final Supplier<Expression> parseGroupedExpression = () -> {
        // Consume Left Paren
        nextToken();

        Expression expression = parseExpression(LOWEST);

        if(!expectPeek(TokenType.R_PAREN)) {
            return null;
        }

        return expression;
    };

    private final Supplier<Expression> parseIfExpression = () -> {
        if(!expectPeek(TokenType.L_PAREN)) return null;

        // consume {
        nextToken();

        Expression condition = parseExpression(LOWEST);

        if(!expectPeek(TokenType.R_PAREN)) return null;

        if(!expectPeek(TokenType.L_BRACE)) return null;

        BlockStatement consequence = parseBlockStatement();

        BlockStatement alternative = null;

        if(peekTokenIs(TokenType.ELSE)) {
            nextToken();

            if(!expectPeek(TokenType.L_BRACE)) return null;

            alternative = parseBlockStatement();
        }

        return new IfExpression(condition, consequence, alternative);
    };

    private final Supplier<Expression> parseFunctionLiteral = () -> {
        if(!expectPeek(TokenType.L_PAREN)) return null;

        List<Identifier> parameters = parseFunctionParameters();

        if(!expectPeek(TokenType.L_BRACE)) return null;

        BlockStatement body = parseBlockStatement();

        return new FunctionLiteral(parameters, body);
    };

    private final Supplier<Expression> parseArrayLiteral = () -> {
        List<Expression> elements = parseExpressionList(TokenType.R_BRACKET);
        return new ArrayLiteral(elements);
    };

    private final Supplier<Expression> parseMapLiteral = () -> {
        Map<Expression, Expression> pairs = new HashMap<>();

        while (!peekTokenIs(TokenType.R_BRACE)) {
            nextToken();
            Expression key = parseExpression(LOWEST);
            if(!expectPeek(TokenType.COLON)) return null;
            nextToken();
            Expression value = parseExpression(LOWEST);
            pairs.put(key, value);

            if(!peekTokenIs(TokenType.R_BRACE) && !expectPeek(TokenType.COMMA)) return null;
        }

        if(!expectPeek(TokenType.R_BRACE)) return null;
        return new MapLiteral(pairs);
    };

    private final Function<Expression, Expression> parseInfixExpression = left -> {
        String operator = currentToken.getLiteral();

        Precedence precedence = currentPrecedence();
        nextToken();
        Expression right = parseExpression(precedence);

        return new InfixExpression(left, operator, right);
    };

    private final Function<Expression, Expression> parseCallExpression = function -> {
        List<Expression> arguments = parseExpressionList(TokenType.R_PAREN);
        return new CallExpression(function, arguments);
    };

    private final Function<Expression, Expression> parseIndexExpression = left -> {
        nextToken();
        Expression index = parseExpression(LOWEST);

        if(!expectPeek(TokenType.R_BRACKET)) return null;

        return new IndexExpression(left, index);
    };

    private void nextToken() {
        currentToken = peekToken;
        peekToken = lexer.nextToken();
    }

    private boolean currentTokenIs(TokenType type) {
        return currentToken.getTokenType() == type;
    }

    private boolean peekTokenIs(TokenType type) {
        return peekToken.getTokenType() == type;
    }

    private boolean expectPeek(TokenType type) {
        if(peekTokenIs(type)) {
            nextToken();
            return true;
        } else {
            peekTokenIs(type);
            return false;
        }
    }

    private Precedence peekPrecedence() {
        return precedences.getOrDefault(peekToken.getTokenType(), LOWEST);
    }

    private Precedence currentPrecedence() {
        return precedences.getOrDefault(currentToken.getTokenType(), LOWEST);
    }

    public List<String> getErrors() {
        return errors;
    }

    private void peekError(TokenType type){
        String message = String.format("expected next token to be %s, got %s instead",
                type,
                peekToken.getTokenType());
        errors.add(message);
    }

    private void noPrefixParseFnError(TokenType tokenType) {
        String message = String.format("no prefix parse function for %s found", tokenType);
        errors.add(message);
    }
}
