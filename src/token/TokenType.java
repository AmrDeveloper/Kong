package token;

public enum  TokenType {

    ILLEGAL,
    EOF,

    // Identifier + literal
    IDENT,
    INT,

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
    SEMICOLON,

    L_PAREN,
    R_PAREN,
    L_BRACE,
    R_BRACE,

    // Keywords
    FUNCTION,
    LET,
    TRUE,
    FALSE,
    IF,
    ELSE,
    RETURN
}
