package dto.employee;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public final class SuperiorEmployeeDTO extends EmployeeBaseDTO {
    private final int workExperience;
}
