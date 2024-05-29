package dto.employee;

import domain.departament.Departament;
import domain.departament.Level;
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
public abstract class EmployeeBaseDTO {
    private Long id;
    private String name;
    private LocalDate birthDate;
    private int age;
    private String document;
    private Map<Departament, Map<Level, BigDecimal>> departamentsAndLevelsAndSalaries;
    private LocalDateTime hireDate;
}
