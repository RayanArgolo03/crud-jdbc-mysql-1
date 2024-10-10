package factory;

import model.Employee;
import model.NormalEmployee;
import model.SuperiorEmployee;
import enums.employee.EmployeeType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

public final class EmployeeBuilderFactory {

    private EmployeeBuilderFactory() {
    }

    public static Employee.Builder<?> newEmployeeBuilder(final EmployeeType type) {
        return switch (type) {
            case NORMAL -> new NormalEmployee.Builder<>();
            case SUPERIOR -> new SuperiorEmployee.Builder<>();
        };
    }
}
