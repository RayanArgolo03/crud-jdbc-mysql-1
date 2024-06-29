package mappers.impl;

import domain.employee.NormalEmployee;
import dto.employee.NormalEmployeeDTO;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import mappers.interfaces.Mapper;

@NoArgsConstructor(access = AccessLevel.PUBLIC)
public final class NormalEmployeeMapperImpl implements Mapper<NormalEmployeeDTO, NormalEmployee> {
    @Override
    public NormalEmployee dtoToEntity(final NormalEmployeeDTO dto) {
        return NormalEmployee.builder().
                id(dto.getId())
                .name(dto.getName())
                .birthDate(dto.getBirthDate())
                .age(dto.getAge())
                .document(dto.getDocument())
                .departmentsAndLevelsAndSalaries(dto.getDepartamentsAndLevelsAndSalaries())
                .hasFaculty(dto.isHasFaculty())
                .hireDate(dto.getHireDate())
                .build();
    }
}
