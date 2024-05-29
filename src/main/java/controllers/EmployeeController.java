package controllers;

import domain.departament.Departament;
import domain.departament.Level;
import domain.employee.Employee;
import enums.employee.EmployeeDeleteOption;
import enums.employee.EmployeeFindOption;
import enums.employee.EmployeeType;
import enums.employee.EmployeeUpdateOption;
import lombok.AllArgsConstructor;
import services.EmployeeService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
public final class EmployeeController {

    private final EmployeeService service;

    public void create(final List<Departament> departaments) {

        String name = service.receiveInputString("first name (without special characters and more than three letters!)");
        name = service.validateAndFormatName(name);

        final String document = service.receiveInputString("document (CPF with dots and dash)");
        service.validateDocument(document);

        final String dateInString = service.receiveInputString("birth date (pattern dd/MM/yyyy)");
        final LocalDate birthDate = service.parseAndValidateDate(dateInString);
        final int age = service.generateAge(birthDate);

        final Map<Departament, Map<Level, BigDecimal>> dls = service.receiveJobsInformation(departaments);

        final EmployeeType type = service.receiveEnumElement("Employee type", EmployeeType.class);
        final Employee employee = service.createEmployee(name, document, birthDate, age, dls, type);
        service.defineSpecificAtributtes(employee);

        //Not commit here, and throw exception if there are problems
        service.saveBaseEmployee(employee);

        //This method closes the connection opened above and commit the changes!
        service.saveSpecificEmployee(employee);
    }

    public List<Employee> find() {
        EmployeeFindOption option = service.receiveEnumElement("find option", EmployeeFindOption.class);
        return service.findByOption(option);
    }

    public Employee chooseEmployeeToUpdate(final List<Employee> employeesFound) {
        return employeesFound.size() == 1
                ? employeesFound.get(0)
                : service.receiveEmployee(employeesFound);
    }

    public void update(final Employee employee) {
        final EmployeeUpdateOption option = service.receiveEnumElement("Option to update", EmployeeUpdateOption.class);
        service.updateByOption(option, employee);
    }

    public int delete(final List<Departament> departaments) {
        final EmployeeDeleteOption option = service.receiveEnumElement("Option to delete", EmployeeDeleteOption.class);
        return service.deleteByOption(option, departaments);
    }
}
