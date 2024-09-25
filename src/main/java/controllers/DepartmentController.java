package controllers;

import model.department.Department;
import enums.departament.DepartmentDeleteOption;
import enums.departament.DepartmentFindOption;
import enums.departament.DepartmentUpdateOption;

import static utils.ReaderUtils.*;
import static utils.EnumListUtils.*;

import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import services.DepartmentService;

import java.util.List;

@Log4j2
@AllArgsConstructor
@FieldDefaults(makeFinal = true)
public final class DepartmentController {

    private DepartmentService service;

    public void create() {

        String name = readString("department departmentName");
        name = service.validateAndFormatName(name);

        final Department department = service.createDepartament(name);
        service.saveDepartament(department);

        log.info("Department created: {}", department);
    }

    public List<Department> findAll() {
        log.info("\n Finding departments..");
        return service.findAll();
    }

    public List<Department> find() {
        final DepartmentFindOption option = readElement("find option",
                getEnumList(DepartmentFindOption.class));
        return service.findByOption(option);
    }

    public Department chooseDepartamentToUpdate(final List<Department> departamentsFound) {
        return departamentsFound.size() == 1
                ? departamentsFound.get(0)
                : readElement("Many employees returned!", departamentsFound);
    }

    public void update(final Department department) {
        //The departament can only have its departmentName updated, but new options can be added
        service.updateByOption(DepartmentUpdateOption.NAME, department);
    }

    public int delete() {
        final DepartmentDeleteOption option = readElement("delete option",
                List.of(DepartmentDeleteOption.values()));
        return service.deleteByOption(option);
    }

}
