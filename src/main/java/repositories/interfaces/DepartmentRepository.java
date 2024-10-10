package repositories.interfaces;

import criteria.DepartmentFilter;
import model.Department;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface DepartmentRepository extends EntityRepository<Department> {
    List<Department> findAll();

    Set<Department> findbyFilters(DepartmentFilter filters);

    Optional<Department> findByDepartmentName(String departmentName);

    Optional<Department> findByCreationDate(LocalDate creationDate);

    Optional<Department> findByUpdateDate(LocalDateTime updateDate);

    Optional<Department> findByUpdateTime(LocalTime updateTime);

    Optional<Department> findByEmployeeName(String employeeName);

    Optional<Department> findByEmployeeAge(Integer employeeAge);

    Optional<Department> findByEmployeeHireDate(LocalDate employeHireDate);

    void updateName(Department department, String newName);

    Optional<Department> findAndDelete(String name);
}
