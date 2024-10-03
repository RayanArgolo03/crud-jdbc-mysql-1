package factory;

import model.Employee;
import model.NormalEmployee;
import model.SuperiorEmployee;
import enums.employee.EmployeeType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EmployeeBuilderFactory {

    public static Employee.Builder<?, ?> newEmployeeBuilder(final EmployeeType type) {
        return switch (type) {
            case NORMAL -> NormalEmployee.builder();
            case SUPERIOR -> SuperiorEmployee.builder();
        };
    }
}
