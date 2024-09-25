package repositories.interfaces;

import model.department.Department;
import dtos.DepartmentResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DepartmentRepository extends EntityRepository<Department> {
    List<Department> findAll();

    List<DepartmentResponse> findByName(String name);

    Optional<DepartmentResponse> findById(long id);

    List<DepartmentResponse> findbyCreationDate(LocalDate creationDateWithoutTime);

    void updateName(Department department, String newName);

    int deleteByName(String name);
}
