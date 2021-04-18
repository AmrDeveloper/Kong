package object;

public class KongError extends KongObject {

    private final String message;

    public KongError(String message) {
        super(ObjectType.ERROR);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String inspect() {
        return "ERROR: " + message;
    }
}
