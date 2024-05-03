package factory;

import domain.employees.Employee;
import domain.employees.NormalEmployee;
import domain.employees.SuperiorEmployee;
import enums.employee.EmployeeType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EmployeeBuilderFactory {
    public static Employee.EmployeeBuilder<?, ?> newEmployeeBuilder(final EmployeeType type) {
        return switch (type) {
            case NORMAL -> NormalEmployee.builder();
            case SUPERIOR -> SuperiorEmployee.builder();
        };
    }
}
