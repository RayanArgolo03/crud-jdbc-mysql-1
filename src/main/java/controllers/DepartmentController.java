package controllers;

import dtos.response.DepartmentResponse;
import enums.department.DepartmentFind;
import enums.department.DepartmentUpdate;
import lombok.extern.log4j.Log4j2;
import model.Department;
import services.DepartmentService;
import utils.ReaderUtils;

import java.util.List;
import java.util.Set;

@Log4j2
public final class DepartmentController {

    private final DepartmentService service;

    public DepartmentController(DepartmentService service) {
        this.service = service;
    }

    public DepartmentService getService() {
        return service;
    }

    public List<Department> findAll() {
        log.info("\n Finding departments..");
        return service.findAll();
    }

    public DepartmentResponse create() {

        final String name = service.validateAndFormatName(
                ReaderUtils.readString("department name (without special characters and more than 2 characters)")
        );

        return service.save(
                new Department(name)
        );

    }

    public Set<DepartmentResponse> findByFilters() {
        return service.findByFilters(
                service.createFilters()
        );
    }

    public Department findByOption() {
        return service.findByOption(
                ReaderUtils.readEnum("option to find", DepartmentFind.class)
        );
    }

    public DepartmentResponse update(final Department department) {
        return service.updateByOption(
                ReaderUtils.readEnum("option to update", DepartmentUpdate.class), department
        );
    }

    public DepartmentResponse findAndDelete() {
        return service.findAndDelete(
                ReaderUtils.readString("department name")
        );
    }

}
