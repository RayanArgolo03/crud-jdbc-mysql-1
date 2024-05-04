package controllers;

import domain.departaments.Departament;
import domain.departaments.Level;
import domain.employees.Employee;
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

        final String name = service.receiveName();
        final String document = service.receiveDocument();

        final LocalDate birthDate = service.receiveDate("birth date (pattern dd/MM/yyyy)");
        final int age = service.generateAge(birthDate);

        final Map<Departament, Map<Level, BigDecimal>> dls = service.receiveJobsInformation(departaments);

        System.out.println("Employee type");
        final EmployeeType type = service.receiveOption(EmployeeType.class);
        final Employee employee = service.createEmployee(name, document, birthDate, age, dls, type);

        //Throw exception if document alredy exists! Not commit here
        service.saveBaseEmployee(employee);

        //This method closes the connection opened above and commit the changes!
        service.saveSpecificEmployee(employee);
    }

    public List<Employee> find(final List<EmployeeFindOption> availableEnums) {
        System.out.println("Receiving option to find..");
        final EmployeeFindOption option = service.receiveOption(availableEnums);
        return service.findByOption(option);
    }

    public Employee chooseEmployeeToUpdate(final List<Employee> employeesFound) {
        return employeesFound.size() == 1
                ? employeesFound.get(0)
                : service.receiveEmployee(employeesFound);
    }

    public void update(final Employee employee) {
        System.out.println("Receiving option to update..");
        final EmployeeUpdateOption option = service.receiveOption(EmployeeUpdateOption.class);
        service.updateByOption(option, employee);
    }

    public int delete(final List<Departament> departaments) {
        System.out.println("Receiving option to delete..");
        final EmployeeDeleteOption option = service.receiveOption(EmployeeDeleteOption.class);
        return service.deleteByOption(option, departaments);
    }
}
