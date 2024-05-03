package dao.interfaces;

import domain.departaments.Departament;
import domain.departaments.Level;
import domain.employees.Employee;
import domain.employees.NormalEmployee;
import domain.employees.SuperiorEmployee;
import dto.employee.EmployeeBaseDTO;

import java.math.BigDecimal;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface EmployeeDAO extends EntityDAO<Employee> {
    void saveJobsInformation(Connection c, long id, Map<Departament, Map<Level, BigDecimal>> dls);

    void saveNormalEmployee(NormalEmployee normalEmployee);

    void saveSuperiorEmployee(SuperiorEmployee superiorEmployee);

    Optional<EmployeeBaseDTO> findById(long id);

    Optional<EmployeeBaseDTO> findByDocument(String document);

    List<EmployeeBaseDTO> findByName(String name);

    List<EmployeeBaseDTO> findByHireDate(LocalDate hireDateWithoutTime);

    List<EmployeeBaseDTO> findByAge(int age);

    void updateName(Employee employee, String newName);

    void updateDocument(Employee employee, String newDocument);

    void updateLevel(Employee employee, Departament departament, Level newLevel);

    void updateSalary(Employee employee, Departament departament, BigDecimal newSalary);

    int deleteByName(String name);

    int deleteByDocument(String document);

    int deleteByHireDate(LocalDate hireDateWithoutTime);
    int deleteByDepartament(Departament departament);

}
