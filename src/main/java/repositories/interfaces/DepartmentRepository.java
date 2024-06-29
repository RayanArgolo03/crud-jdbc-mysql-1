package repositories.interfaces;

import domain.department.Department;
import dto.departament.DepartmentDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DepartmentRepository extends EntityRepository<Department> {
    List<DepartmentDTO> findAll();

    List<DepartmentDTO> findByName(String name);

    Optional<DepartmentDTO> findById(long id);

    List<DepartmentDTO> findbyCreationDate(LocalDate creationDateWithoutTime);

    void updateName(Department department, String newName);

    int deleteByName(String name);
}
