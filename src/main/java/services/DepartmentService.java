package services;

import domain.department.Department;
import dto.departament.DepartmentDTO;
import enums.departament.DepartmentDeleteOption;
import enums.departament.DepartmentFindOption;
import enums.departament.DepartmentUpdateOption;
import exceptions.DbConnectionException;
import exceptions.DepartmentException;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import mappers.interfaces.Mapper;
import repositories.interfaces.DepartmentRepository;
import utils.FormatterUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static utils.ReaderUtils.readLong;
import static utils.ReaderUtils.readString;

@FieldDefaults(makeFinal = true)
@AllArgsConstructor
public final class DepartmentService {

    private DepartmentRepository repository;
    private Mapper<DepartmentDTO, Department> mapper;

    public String validateAndFormatName(final String name) {
        Objects.requireNonNull(name, "Name can´t be null!");
        return FormatterUtils.formatName(name);
    }

    public LocalDate parseAndValidateDate(final String dateInString) {
        Objects.requireNonNull(dateInString, "Creation date can´t be null");
        try {
            return LocalDate.parse(dateInString, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (DateTimeParseException e) {
            throw new DepartmentException(String.format("Invalid date! %s does not match the pattern dd/MM/yyyy!", dateInString), e);
        }
    }

    public Department createDepartament(final String name) {
        return Department.builder()
                .name(name)
                .creationDate(LocalDateTime.now())
                //Not yet received updates
                .lastUpdateDate(null)
                .build();
    }

    public void saveDepartament(final Department department) {
        Objects.requireNonNull(department, "Department can´t be null!");
        try {
            repository.save(department);
        } catch (DbConnectionException e) {
            throw new DepartmentException(String.format("Error: %s", e.getMessage()), e);
        }
    }


    public List<Department> findAll() {

        final List<DepartmentDTO> list = repository.findAll();
        if (list.isEmpty()) throw new DepartmentException("No has departments!");

        return list.stream()
                .map(mapper::dtoToEntity)
                .collect(Collectors.toList());
    }

    public List<Department> findByOption(final DepartmentFindOption option) {

        return switch (option) {
            case ID -> {
                final long id = readLong("id");
                yield Collections.singletonList(this.findById(id));
            }
            case NAME -> {
                final String name = readString("name");
                yield this.findByName(name);
            }
            case CREATION_DATE -> {
                final LocalDate creationDateWithoutTime = parseAndValidateDate(
                        readString("creation date (pattern dd/mm/yyyy)")
                );
                yield this.findByCreationDate(creationDateWithoutTime);
            }
        };
    }

    public Department findById(final long id) {
        return repository.findById(id)
                .map(mapper::dtoToEntity)
                .orElseThrow(() -> new DepartmentException(String.format("Department with id %d not found!", id)));
    }

    public List<Department> findByName(final String name) {

        Objects.requireNonNull(name, "Name can´t be null!");

        final List<DepartmentDTO> departaments = repository.findByName(name);
        if (departaments.isEmpty())
            throw new DepartmentException(String.format("Departments not found by name %s!", name));

        return departaments.stream()
                .map(mapper::dtoToEntity)
                .collect(Collectors.toList());
    }

    public List<Department> findByCreationDate(final LocalDate creationDateWithoutTime) {

        final List<DepartmentDTO> departaments = repository.findbyCreationDate(creationDateWithoutTime);
        if (departaments.isEmpty()) throw new DepartmentException("Departaments not found!");

        return departaments.stream()
                .map(mapper::dtoToEntity)
                .collect(Collectors.toList());
    }

    public void updateByOption(final DepartmentUpdateOption option,
                               final Department department) {

        //Open for extensions
        switch (option) {
            case NAME -> {
                String newName = readString("new name");
                this.updateName(department, newName);
            }
        }

    }

    public void updateName(final Department department, final String newName) {

        Objects.requireNonNull(newName, "New name can´t be null!");

        if (newName.equals(department.getName())) throw new DepartmentException("New name is equals to current name!");

        repository.updateName(department, newName);
    }

    public int deleteByOption(final DepartmentDeleteOption departmentDeleteOption) {

        return switch (departmentDeleteOption) {
            case ID -> {
                final long id = readLong("id");
                yield this.deleteById(id);
            }
            case NAME -> {
                final String name = readString("name");
                yield this.deleteByName(name);
            }
        };

    }

    public int deleteById(final long id) {
        try {
            return repository.deleteById(id);
        } catch (DbConnectionException e) {
            throw new DepartmentException(String.format("Error: %s", e.getMessage()), e);
        }
    }

    public int deleteByName(final String name) {
        Objects.requireNonNull(name, "Name can´t be null!");
        try {
            return repository.deleteByName(name);
        } catch (DbConnectionException e) {
            throw new DepartmentException(String.format("Error: %s", e.getMessage()), e);
        }
    }

}
