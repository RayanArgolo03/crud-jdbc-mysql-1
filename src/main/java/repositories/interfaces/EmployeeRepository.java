package repositories.interfaces;

import domain.department.Department;
import domain.department.Level;
import domain.employee.Employee;
import domain.employee.NormalEmployee;
import domain.employee.SuperiorEmployee;
import dto.employee.EmployeeBaseDTO;

import java.math.BigDecimal;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface EmployeeRepository extends EntityRepository<Employee> {
    void saveJobsInformation(Connection c, long id, Map<Department, Map<Level, BigDecimal>> dls);

    void saveNormalEmployee(NormalEmployee ne);

    void saveSuperiorEmployee(SuperiorEmployee se);

    Optional<EmployeeBaseDTO> findById(long employeeId);

    Optional<EmployeeBaseDTO> findByDocument(String document);

    List<EmployeeBaseDTO> findByName(String name);

    List<EmployeeBaseDTO> findByHireDate(LocalDate hireDateWithoutTime);

    List<EmployeeBaseDTO> findByAge(int age);

    void updateName(Employee employee, String newName);

    void updateDocument(Employee employee, String newDocument);

    void updateLevel(Employee employee, Department department, Level newLevel, Level oldLevel);

    void updateSalary(Employee employee, Department department, BigDecimal newSalary, BigDecimal oldSalary);

    int deleteByName(String name);

    int deleteByDocument(String document);

    int deleteByHireDate(LocalDate hireDateWithoutTime);

    int deleteByDepartment(Department department);

}
