package mappers;

import domain.employee.SuperiorEmployee;
import dto.employee.SuperiorEmployeeDTO;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PUBLIC)
public final class SuperiorEmployeeMapper {
    public SuperiorEmployee dtoToEntity(final SuperiorEmployeeDTO dto) {
        return SuperiorEmployee.builder()
                .id(dto.getId())
                .name(dto.getName())
                .birthDate(dto.getBirthDate())
                .age(dto.getAge())
                .document(dto.getDocument())
                .departamentsAndLevelsAndSalaries(dto.getDepartamentsAndLevelsAndSalaries())
                .workExperience(dto.getWorkExperience())
                .hireDate(dto.getHireDate())
                .build();
    }
}
