package dto.base;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@AllArgsConstructor

//Using in classes with Builder Pattern
@SuperBuilder
@Getter
public abstract class BaseDto {
    private Long id;
}
