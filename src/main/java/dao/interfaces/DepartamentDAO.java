package dao.interfaces;

import domain.departament.Departament;
import dto.departament.DepartamentDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DepartamentDAO extends EntityDAO<Departament> {
    List<DepartamentDTO> findAll();

    List<DepartamentDTO> findByName(String name);

    Optional<DepartamentDTO> findById(long id);

    List<DepartamentDTO> findbyCreationDate(LocalDate creationDateWithoutTime);

    void updateName(Departament departament, String newName);

    int deleteByName(String name);
}
