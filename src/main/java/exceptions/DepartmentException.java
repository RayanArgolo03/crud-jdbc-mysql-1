package exceptions;

import java.io.Serial;

public final class DepartmentException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -4930651273871488755L;

    public DepartmentException(String message) {
        super(message);
    }

    public DepartmentException(String message, Throwable cause) {
        super(message, cause);
    }
}
