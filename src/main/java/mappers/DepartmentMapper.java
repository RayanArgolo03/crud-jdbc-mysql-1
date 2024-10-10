package mappers;

import dtos.request.DepartmentRequest;
import dtos.response.DepartmentResponse;
import model.Department;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import utils.FormatterUtils;

@Mapper(imports = {FormatterUtils.class})
public interface DepartmentMapper {

    @Mapping(target = "createdDate", expression = "java(FormatterUtils.formatDate(department.getCreatedDate()))")
    @Mapping(target = "lastUpdate", expression = "java(FormatterUtils.formatDate(department.getLastUpdateDate()))")
    @Mapping(target = "employees", expression = "java(department.getEmployees())")
    DepartmentResponse departmentToResponse(Department department);

    @Mapping(target = "name", source = "name")
    Department requestToDepartment(DepartmentRequest request);


}
