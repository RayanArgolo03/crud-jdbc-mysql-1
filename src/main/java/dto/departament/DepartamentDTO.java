package dto.departament;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Builder
@FieldDefaults(makeFinal = true)
@Getter
public final class DepartamentDTO {
    private Long id;
    private String name;
    private LocalDateTime creationDate;
    private LocalDateTime lastUpdateDate;
}


