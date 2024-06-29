package mappers.impl;

import domain.department.Department;
import dto.departament.DepartmentDTO;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import mappers.interfaces.Mapper;

@NoArgsConstructor(access = AccessLevel.PUBLIC)
public final class DepartmentMapperImpl implements Mapper<DepartmentDTO, Department> {
    @Override
    public Department dtoToEntity(final DepartmentDTO dto) {
        return Department.builder()
                .id(dto.getId())
                .name(dto.getName())
                .creationDate(dto.getCreationDate())
                .lastUpdateDate(dto.getLastUpdateDate())
                .build();
    }
}
