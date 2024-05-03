package exceptions;

import java.io.Serial;

public final class UserException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -4930651273871488755L;

    public UserException(String message) {
        super(message);
    }
}
