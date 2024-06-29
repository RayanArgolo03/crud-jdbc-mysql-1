package dto.departament;

import dto.base.BaseDto;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@SuperBuilder
@FieldDefaults(makeFinal = true)
@Getter
public final class DepartmentDTO extends BaseDto {
    private String name;
    private LocalDateTime creationDate;
    private LocalDateTime lastUpdateDate;
}


