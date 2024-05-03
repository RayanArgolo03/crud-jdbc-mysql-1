package mappers;

import domain.departaments.Departament;
import dto.departament.DepartamentDTO;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PUBLIC)
public final class DepartamentMapper {
    public Departament dtoToEntity(final DepartamentDTO dto) {
        return Departament.builder()
                .id(dto.getId())
                .name(dto.getName())
                .creationDate(dto.getCreationDate())
                .lastUpdateDate(dto.getLastUpdateDate())
                .build();
    }
}
