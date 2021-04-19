package token;

public enum  TokenType {

    ILLEGAL,
    EOF,

    // Identifier + literal
    IDENT,
    INT,
    STRING,

    // Operators
    ASSIGN,
    PLUS,
    MINUS,
    BANG,
    ASTERISK,
    SLASH,

    EQ,
    NOT_EQ,
    LT,
    GT,

    // Delimiters
    COMMA,
    COLON,
    SEMICOLON,

    L_PAREN,
    R_PAREN,
    L_BRACE,
    R_BRACE,
    L_BRACKET,
    R_BRACKET,

    // Keywords
    FUNCTION,
    LET,
    TRUE,
    FALSE,
    IF,
    ELSE,
    RETURN
}
