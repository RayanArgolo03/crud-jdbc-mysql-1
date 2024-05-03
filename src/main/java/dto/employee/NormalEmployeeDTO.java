package dto.employee;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public final class NormalEmployeeDTO extends EmployeeBaseDTO {
    private final boolean hasFaculty;
}
