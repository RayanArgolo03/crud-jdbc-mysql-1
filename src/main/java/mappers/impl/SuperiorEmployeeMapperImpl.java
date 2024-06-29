package mappers.impl;

import domain.employee.SuperiorEmployee;
import dto.employee.SuperiorEmployeeDTO;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import mappers.interfaces.Mapper;

@NoArgsConstructor(access = AccessLevel.PUBLIC)
public final class SuperiorEmployeeMapperImpl implements Mapper<SuperiorEmployeeDTO, SuperiorEmployee> {
    @Override
    public SuperiorEmployee dtoToEntity(final SuperiorEmployeeDTO dto) {
        return SuperiorEmployee.builder()
                .id(dto.getId())
                .name(dto.getName())
                .birthDate(dto.getBirthDate())
                .age(dto.getAge())
                .document(dto.getDocument())
                .departmentsAndLevelsAndSalaries(dto.getDepartamentsAndLevelsAndSalaries())
                .workExperience(dto.getWorkExperience())
                .hireDate(dto.getHireDate())
                .build();
    }
}
