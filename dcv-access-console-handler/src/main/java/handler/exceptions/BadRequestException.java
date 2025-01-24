package handler.exceptions;

public class BadRequestException extends RuntimeException {
    private final String message;

    public BadRequestException(String err) {
        super(err);
        message = err;
    }

    public BadRequestException(Throwable err) {
        super(err);
        message = err.getMessage();
    }

    public String getMessage() {
        return message;
    }
}