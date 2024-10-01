package exceptions;

import java.io.Serial;

public final class DatabaseException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1197976454952968103L;

    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
