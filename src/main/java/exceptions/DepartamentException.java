package exceptions;

import java.io.Serial;

public final class DepartamentException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -4930651273871488755L;

    public DepartamentException(String message) {
        super(message);
    }
}
