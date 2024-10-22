package controllers;

import dtos.response.EmployeeResponse;
import enums.employee.EmployeeType;
import factory.EmployeeBuilderFactory;
import model.Department;
import model.Employee;
import model.Job;
import services.EmployeeService;
import utils.ReaderUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static utils.ReaderUtils.readEnum;
import static utils.ReaderUtils.readString;

public final class EmployeeController {

    private final EmployeeService service;

    public EmployeeController(EmployeeService service) {
        this.service = service;
    }

    public EmployeeService getService() {
        return service;
    }

    public EmployeeResponse create(final List<Department> departments) {

        final String name = service.validateAndFormatName(
                readString("first name (without special characters and more than three letters!)")
        );

        final String document = service.validateAndFormatDocument(
                readString("CPF (patern xxx.xxx.xxx-xx with symbols)")
        );

        final LocalDate birthDate = service.parseAndValidateTemporal(
                readString("birth date (pattern dd/MM/yyyy)"),
                "dd/MM/uuuu",
                LocalDate::from
        );

        final int age = service.generateAge(birthDate);

        final EmployeeType type = readEnum("employee type", EmployeeType.class);

        final Employee employee = EmployeeBuilderFactory.newEmployeeBuilder(type)
                .name(name)
                .document(document)
                .birthDate(birthDate)
                .age(age)
                .build();

        final Set<Job> jobs = service.createJobs(departments, employee);
        employee.setJobs(jobs);

        service.defineSpecificAtributtes(employee);

        return service.save(employee);

    }

    public Set<EmployeeResponse> findByFilters() {
        return service.findByFilters();
    }

    public Employee find() {
        return service.findByName(
                ReaderUtils.readString("first name")
        );
    }

    public EmployeeResponse update(final Employee employee) {
        return service.updateByOption(employee);
    }

    public void delete(final Employee employee) {
        service.delete(employee);
    }
}
