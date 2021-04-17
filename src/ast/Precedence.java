package ast;

public enum Precedence {
    LOWEST(1),
    EQUALS(2), // ==
    LESSGREATER(3), // > or <
    SUM(4), // +
    PRODUCT(5), // *
    PREFIX(6),// -X or !X
    CALL(7); // myFunction(X)

    private final int rank;

    Precedence(int rank) {
        this.rank = rank;
    }

    public int getRank() {
        return rank;
    }
}
