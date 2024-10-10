package repositories.interfaces;

import criteria.EmployeeFilter;
import model.Employee;

import java.util.Optional;
import java.util.Set;

public interface EmployeeRepository extends EntityRepository<Employee> {

    Set<Employee> findByFilters(EmployeeFilter filters);
    Optional<Employee> findByName(String name);

    void update(Employee employee);

    void deleteByName(String name);

}
