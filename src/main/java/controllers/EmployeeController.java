package controllers;

import enums.employee.EmployeeDelete;
import enums.employee.EmployeeFind;
import enums.employee.EmployeeType;
import enums.employee.EmployeeUpdate;
import model.Department;
import model.Level;
import model.Employee;
import services.EmployeeService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static utils.EnumListUtils.getEnumList;
import static utils.ReaderUtils.readString;

public final class EmployeeController {

    private final EmployeeService service;

    public EmployeeController(EmployeeService service) {
        this.service = service;
    }

    public void create(final Set<Department> departments) {

        final String name = service.validateAndFormatName(
                readString("first name (without special characters and more than three letters!)")
        );

        final String document = service.validateAndFormatDocument(
                readString("CPF (patern xxx.xxx.xxx-xx with symbols)")
        );

        final LocalDate birthDate = service.parseAndValidateDate(
                readString("birth date (pattern dd/MM/yyyy)")
        );

        final int age = service.generateAge(birthDate);

        //Todo
        final Map<Department, Map<Level, BigDecimal>> dls = service.receiveJobsInformation(departments);

        final EmployeeType type = readElement("Employee type",
                getEnumList(EmployeeType.class));
        final Employee employee = service.createEmployee(name, document, birthDate, age, dls, type);
        service.defineSpecificAtributtes(employee);

        //Not commit here, and throw exception if there are problems
        service.saveBaseEmployee(employee);

        //This method closes the connection opened above and commit the changes!
        service.saveSpecificEmployee(employee);
    }

    public List<Employee> find() {
        final EmployeeFind option = readElement("find option",
                getEnumList(EmployeeFind.class));
        return service.findByOption(option);
    }

    public Employee chooseEmployeeToUpdate(final List<Employee> employeesFound) {
        return employeesFound.size() == 1
                ? employeesFound.get(0)
                : readElement("Many employees returned!",
                employeesFound);
    }

    public void update(final Employee employee) {
        final EmployeeUpdate option = readElement("Option to update",
                getEnumList(EmployeeUpdate.class));
        service.updateByOption(option, employee);
    }

    public int delete(final Set<Department> departments) {
        final EmployeeDelete option = readElement("Option to delete",
                getEnumList(EmployeeDelete.class));
        return service.deleteByOption(option, departments);
    }
}
