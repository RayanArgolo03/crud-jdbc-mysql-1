package controllers;

import enums.employee.EmployeeDeleteOption;
import enums.employee.EmployeeFindOption;
import enums.employee.EmployeeType;
import enums.employee.EmployeeUpdateOption;
import model.department.Department;
import model.department.Level;
import model.employee.Employee;
import services.EmployeeService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static utils.EnumListUtils.getEnumList;
import static utils.ReaderUtils.readElement;
import static utils.ReaderUtils.readString;

public final class EmployeeController {

    private final EmployeeService service;

    public EmployeeController(EmployeeService service) {
        this.service = service;
    }

    public void create(final List<Department> departments) {

        String name = readString("first departmentName (without special characters and more than three letters!)");
        name = service.validateAndFormatName(name);

        final String document = readString("CPF (patern xxx.xxx.xxx-xx)");
        service.validateDocument(document);

        final String dateInString = readString("birth date (pattern dd/MM/yyyy)");
        final LocalDate birthDate = service.parseAndValidateDate(dateInString);
        final int age = service.generateAge(birthDate);

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
        final EmployeeFindOption option = readElement("find option",
                getEnumList(EmployeeFindOption.class));
        return service.findByOption(option);
    }

    public Employee chooseEmployeeToUpdate(final List<Employee> employeesFound) {
        return employeesFound.size() == 1
                ? employeesFound.get(0)
                : readElement("Many employees returned!",
                employeesFound);
    }

    public void update(final Employee employee) {
        final EmployeeUpdateOption option = readElement("Option to update",
                getEnumList(EmployeeUpdateOption.class));
        service.updateByOption(option, employee);
    }

    public int delete(final List<Department> departments) {
        final EmployeeDeleteOption option = readElement("Option to delete",
                getEnumList(EmployeeDeleteOption.class));
        return service.deleteByOption(option, departments);
    }
}
