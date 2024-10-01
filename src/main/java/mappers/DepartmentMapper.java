package mappers;

import dtos.request.DepartmentRequest;
import dtos.response.DepartmentResponse;
import model.department.Department;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import utils.FormatterUtils;

@Mapper(imports = {FormatterUtils.class})
public interface DepartmentMapper {

    @Mapping(target = "departmentName", source = "name")
    @Mapping(target = "createdDate", expression = "java(FormatterUtils.formatDate(department.getCreatedDate()))")
    @Mapping(target = "lastUpdateDate", expression = "java(FormatterUtils.formatDate(department.getLastUpdateDate()))")
    DepartmentResponse departmentToResponse(Department department);

    Department requestToDepartment(DepartmentRequest request);


}
