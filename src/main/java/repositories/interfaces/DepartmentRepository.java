package repositories.interfaces;

import criteria.DepartmentFilter;
import model.department.Department;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import java.util.Set;

public interface DepartmentRepository extends EntityRepository<Department> {
    Set<Department> findAll();

    Set<Department> findbyFilters(DepartmentFilter filters);

    Optional<Department> findByDepartmentName(String departmentName);

    Optional<Department> findByCreationDate(LocalDate creationDate);

    Optional<Department> findByUpdateDate(LocalDateTime updateDate);

    Optional<Department> findByUpdateTime(LocalTime updateTime);

    Optional<Department> findByEmployeeName(String employeeName);

    Optional<Department> findByEmployeeAge(Integer employeeAge);

    Optional<Department> findByEmployeeHireDate(LocalDate employeHireDate);

    Department updateName(Department department, String newName);

    Optional<Department> findAndDelete(String name);
}
