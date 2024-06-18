package mappers;

import domain.employee.NormalEmployee;
import dto.employee.NormalEmployeeDTO;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PUBLIC)
public final class NormalEmployeeMapper {
    public NormalEmployee dtoToEntity(final NormalEmployeeDTO dto) {
        return NormalEmployee.builder().
                id(dto.getId())
                .name(dto.getName())
                .birthDate(dto.getBirthDate())
                .age(dto.getAge())
                .document(dto.getDocument())
                .departamentsAndLevelsAndSalaries(dto.getDepartamentsAndLevelsAndSalaries())
                .hasFaculty(dto.isHasFaculty())
                .hireDate(dto.getHireDate())
                .build();
    }
}
