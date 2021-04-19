package lexer;

import token.Token;
import token.TokenType;

import java.util.HashMap;
import java.util.Map;

public class Lexer {

    private final String input;

    // current position in input (points to current char)
    private int position;

    // current reading position in input (after current char)
    private int readPosition;

    // current char under examination
    private char ch;

    private static final Map<String, TokenType> keywords = new HashMap<>();

    static {
        keywords.put("fn", TokenType.FUNCTION);
        keywords.put("let", TokenType.LET);
        keywords.put("true", TokenType.TRUE);
        keywords.put("false", TokenType.FALSE);
        keywords.put("if", TokenType.IF);
        keywords.put("else", TokenType.ELSE);
        keywords.put("return", TokenType.RETURN);
    }

    public Lexer(String input) {
        this.input = input;
        readChar();
    }

    public Token nextToken() {
        Token token;
        skipWhitespace();
        switch (ch) {
            case '=': {
                if (peekChar() == '=') {
                    char currentChar = ch;
                    readChar();
                    token =  newToken(TokenType.EQ, currentChar + "" + ch);
                } else {
                    token =  newToken(TokenType.ASSIGN, ch);
                }
                break;
            }
            case '(': token =  newToken(TokenType.L_PAREN, ch); break;
            case ')': token =  newToken(TokenType.R_PAREN, ch); break;
            case '{': token =  newToken(TokenType.L_BRACE, ch); break;
            case '}': token =  newToken(TokenType.R_BRACE, ch); break;
            case '[': token =  newToken(TokenType.L_BRACKET, ch); break;
            case ']': token =  newToken(TokenType.R_BRACKET, ch); break;
            case '+': token =  newToken(TokenType.PLUS, ch); break;
            case '-': token =  newToken(TokenType.MINUS, ch); break;
            case '!': {
                if (peekChar() == '=') {
                    char currentChar = ch;
                    readChar();
                    token =  newToken(TokenType.NOT_EQ, currentChar + "" + ch);
                }else {
                    token =  newToken(TokenType.BANG, ch);
                }
                break;
            }
            case '/': token =  newToken(TokenType.SLASH, ch); break;
            case '*': token =  newToken(TokenType.ASTERISK, ch); break;
            case '<': token =  newToken(TokenType.LT, ch); break;
            case '>': token =  newToken(TokenType.GT, ch); break;
            case ';': token =  newToken(TokenType.SEMICOLON, ch); break;
            case ',': token =  newToken(TokenType.COMMA, ch); break;
            case  0 : token =  newToken(TokenType.EOF, ' '); break;
            case '\"': {
                String literal = readString();
                return newToken(TokenType.STRING, literal);
            }
            default: {
                if(isLetter(ch)) {
                    String literal = readIdentifier();
                    TokenType tokenType = lookupIdent(literal);
                    return newToken(tokenType, literal);
                }
                else if(isDigit(ch)) {
                    String literal = readNumber();
                    return newToken(TokenType.INT, literal);
                }
                else {
                    token = newToken(TokenType.ILLEGAL, ch);
                }
            }
        }
        readChar();
        return token;
    }

    private void readChar() {
        if (readPosition >= input.length()) {
            ch = 0;
        } else {
            ch = input.charAt(readPosition);
        }

        position = readPosition;
        readPosition += 1;
    }

    private char peekChar() {
        if(position >= input.length()) return 0;
        return input.charAt(readPosition);
    }

    private void skipWhitespace() {
        while (ch == ' ' || ch == '\t' || ch == '\n' || ch == '\r') {
            readChar();
        }
    }

    private String readIdentifier() {
        int startPosition = readPosition;
        while (isLetter(ch)) {
            readChar();
        }
        return input.substring(startPosition - 1, position);
    }

    private String readNumber() {
        int startPosition = position;

        while (isDigit(ch)) {
            readChar();
        }
        return input.substring(startPosition, position);
    }

    private String readString() {
        int startPosition = position + 1;
        do {
            readChar();
        } while (ch != '"' && ch != 0);

        // Consume closing "
        readChar();
        return input.substring(startPosition, position - 1);
    }

    private TokenType lookupIdent(String ident) {
        return keywords.getOrDefault(ident, TokenType.IDENT);
    }

    private Token newToken(TokenType type, String literal) {
        return new Token(type, literal);
    }

    private Token newToken(TokenType type, char ch) {
        return new Token(type, String.valueOf(ch));
    }

    private boolean isLetter(char ch) {
        return 'a' <= ch && ch <= 'z' || 'A' <= ch && ch <= 'Z' || ch == '_';
    }

    private boolean isDigit(char ch) {
        return '0' <= ch && ch <= '9';
    }

}