package token;

public class Token {

    private final TokenType tokenType;
    private final String literal;

    public Token(TokenType tokenType, String literal) {
        this.tokenType = tokenType;
        this.literal = literal;
    }

    public TokenType getTokenType() {
        return tokenType;
    }

    public String getLiteral() {
        return literal;
    }

    @Override
    public String toString() {
        return "Token {" + "tokenType=" + tokenType + ", literal='" + literal + '\'' + '}';
    }
}


