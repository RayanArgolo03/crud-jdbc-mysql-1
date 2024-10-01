package mappers;

import dtos.response.EmployeeResponse;
import model.employee.Employee;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import utils.FormatterUtils;

@Mapper(imports = {FormatterUtils.class})
public interface EmployeeMapper {

    @Mapping(target = "birthDate", source = "java(FormatterUtils.formatDate(employee.getBirthDate()))")
    @Mapping(target = "createdDate", source = "java(FormatterUtils.formatDate(employee.getCreatedDate()))")
    @Mapping(target = "lastUpdateDate", source = "java(FormatterUtils.formatDate(employee.getLastUpdateDate()))")
    EmployeeResponse employeeToResponse(Employee employee);

}
