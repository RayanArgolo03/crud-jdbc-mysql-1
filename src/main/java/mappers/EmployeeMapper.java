package mappers;

import dtos.response.EmployeeResponse;
import model.Employee;
import model.NormalEmployee;
import model.SuperiorEmployee;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.SubclassExhaustiveStrategy;
import utils.FormatterUtils;

@Mapper(imports = {FormatterUtils.class, NormalEmployee.class, SuperiorEmployee.class}, subclassExhaustiveStrategy = SubclassExhaustiveStrategy.RUNTIME_EXCEPTION)
public interface EmployeeMapper {

    //Mapping shared attributes
    @Mapping(target = "birthDate", expression = "java(FormatterUtils.formatDate(employee.getBirthDate()))")
    @Mapping(target = "createdDate", expression = "java(FormatterUtils.formatDate(employee.getHireDate()))")
    @Mapping(target = "lastUpdate", expression = "java(FormatterUtils.formatDate(employee.getLastUpdateDate()))")

    //Mapping specific attributes
    @Mapping(target = "hasFaculty", expression = "java( (employee instanceof NormalEmployee ne) ? ne.isHasFaculty() : null )")
    @Mapping(target = "workExperience", expression = "java( (employee instanceof SuperiorEmployee se) ? se.getWorkExperience() : null )")
    EmployeeResponse employeeToResponse(Employee employee);

}
