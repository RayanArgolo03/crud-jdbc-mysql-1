package dto.employee;

import domain.department.Department;
import domain.department.Level;
import dto.base.BaseDto;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Getter
@SuperBuilder
@FieldDefaults(makeFinal = true)
public abstract class EmployeeBaseDTO extends BaseDto {
    private String name;
    private LocalDate birthDate;
    private int age;
    private String document;
    private Map<Department, Map<Level, BigDecimal>> departamentsAndLevelsAndSalaries;
    private LocalDateTime hireDate;
}
