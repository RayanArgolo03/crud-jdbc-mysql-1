package exceptions;

import java.io.Serial;

public final class EmployeeException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1197976454952968103L;

    public EmployeeException(String message) {
        super(message);
    }

    //Use in unit tests
    public EmployeeException(String message, Throwable cause) {
        super(message, cause);
    }
}
