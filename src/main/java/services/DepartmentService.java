package services;

import criteria.DepartmentFilter;
import dtos.request.DepartmentRequest;
import dtos.response.DepartmentResponse;
import enums.department.DepartmentFind;
import enums.department.DepartmentUpdate;
import exceptions.DatabaseException;
import exceptions.DepartmentException;
import lombok.extern.log4j.Log4j2;
import mappers.DepartmentMapper;
import model.Department;
import org.hibernate.exception.ConstraintViolationException;
import repositories.interfaces.DepartmentRepository;
import utils.FormatterUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQuery;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static utils.ReaderUtils.readEnum;
import static utils.ReaderUtils.readString;

@Log4j2
public final class DepartmentService {

    private final DepartmentRepository repository;
    private final DepartmentMapper mapper;

    public DepartmentService(DepartmentRepository repository, DepartmentMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public DepartmentMapper getMapper() {
        return mapper;
    }

    public List<Department> findAll() {

        final List<Department> list = repository.findAll();
        if (list.isEmpty()) throw new DepartmentException("No departments in the database!");

        return list;
    }

    public DepartmentResponse save(final DepartmentRequest request) {

        final Department department = mapper.requestToDepartment(request);

        try {
            repository.save(department);

            return mapper.departmentToResponse(department);

        } catch (ConstraintViolationException e) {
            throw new DepartmentException(format("Departmnent with name %s already exists!", department.getName()));
        } catch (Exception e) {
            throw new DepartmentException(format("Error occured on create department: %s", e.getMessage()), e.getCause());
        }
    }

    public Set<DepartmentResponse> findByFilters() {

        final DepartmentFilter filters = createFilters();
        Objects.requireNonNull(filters, "No filters to find departments!");

        final Set<Department> departments = repository.findbyFilters(filters);
        if (departments.isEmpty()) throw new DepartmentException("Departments not found by filters!");

        return departments.stream()
                .map(mapper::departmentToResponse)
                .collect(Collectors.toSet());
    }

    public DepartmentFilter createFilters() {

        final DepartmentFilter filters = new DepartmentFilter();

        DepartmentFind option = null;
        do {
            try {

                switch ((option = readEnum("filter to find", DepartmentFind.class))) {

                    case DEPARTMENT_NAME -> {

                        final String departmentName = readString("department name");
                        if (!departmentName.equalsIgnoreCase(filters.getDepartmentName())) {
                            filters.setDepartmentName(departmentName.toUpperCase());
                        }

                    }
                    case CREATION_DATE -> {

                        final LocalDate createdDate = validadeAndFormatDate(
                                readString("creation date (pattern DD/MM/YYYY with bars symbols)"),
                                "dd/MM/uuuu",
                                LocalDate::from
                        );
                        if (createdDate != null && !createdDate.isEqual(filters.getCreationDate())) {
                            filters.setCreationDate(createdDate);
                        }

                    }
                    case UPDATE_DATE -> {

                        final LocalDateTime updateDate = validadeAndFormatDate(
                                readString("creation date (pattern DD/MM/YYYY HH:MM with symbols)"),
                                "uuuu/MM/dd HH:mm",
                                LocalDateTime::from
                        );
                        //Removing miliseconds to comparison in database
                        if (updateDate != null && !updateDate.isEqual(filters.getLastUpdateDate())) {
                            filters.setLastUpdateDate(updateDate.truncatedTo(ChronoUnit.MICROS));
                        }
                    }
                    case UPDATE_TIME -> {

                        final LocalTime updateTime = validadeAndFormatDate(
                                readString("update time (pattern HH:MM with symbol)"),
                                "HH:mm",
                                LocalTime::from
                        );
                        if (updateTime != null && !updateTime.equals(filters.getLastUpdateTime())) {
                            filters.setLastUpdateTime(updateTime);
                        }
                    }
                    case EMPLOYEE_NAME -> {

                        final String employeeName = readString("employee name");
                        if (!employeeName.equalsIgnoreCase(filters.getEmployeeName())) {
                            filters.setEmployeeName(employeeName.toUpperCase());
                        }

                    }
                    case EMPLOYEE_AGE -> {

                        final Integer employeeAge = validateAndFormatEmployeeAge(readString("employee age (over 17 years old)"));
                        if (employeeAge != null && !employeeAge.equals(filters.getEmployeeAge())) {
                            filters.setEmployeeAge(employeeAge);
                        }

                    }
                    case EMPLOYEE_HIRE_DATE -> {

                        final LocalDate employeeHireDate = validadeAndFormatDate(
                                readString("update time (pattern DD/MM/YYYY with symbols)"),
                                "dd/MM/uuuu",
                                LocalDate::from
                        );
                        if (employeeHireDate != null && !employeeHireDate.equals(filters.getEmployeeHireDate())) {
                            filters.setEmployeeHireDate(employeeHireDate);
                        }

                    }
                }

            } catch (InputMismatchException e) {
                log.error("Invalid value!");

            } catch (Exception e) {
                log.error(e.getMessage());
            }

        } while (option != DepartmentFind.OUT);

        if (!filters.hasFilters()) return null;

        return filters;
    }

    public Department findByOption(final DepartmentFind option) {

        final Optional<Department> department = switch (option) {

            case DEPARTMENT_NAME -> repository.findByDepartmentName(readString("department name").toUpperCase());

            case CREATION_DATE -> repository.findByCreationDate(
                    validadeAndFormatDate(
                            readString("creation date (pattern DD/MM/YYYY with bars symbols)"),
                            "uuuu/MM/dd",
                            LocalDate::from
                    )
            );

            //Removing miliseconds to comparison in database
            case UPDATE_DATE -> repository.findByUpdateDate(
                    validadeAndFormatDate(
                            readString("creation date (pattern DD/MM/YYYY HH:MM with symbols)"),
                            "uuuu/MM/dd HH:mm",
                            LocalDateTime::from
                    ).truncatedTo(ChronoUnit.MICROS)

            );

            case UPDATE_TIME -> repository.findByUpdateTime(
                    validadeAndFormatDate(
                            readString("update time (pattern HH:MM with symbol)"),
                            "HH:mm",
                            LocalTime::from
                    )
            );

            case EMPLOYEE_NAME -> repository.findByEmployeeName(readString("employee name").toUpperCase());

            case EMPLOYEE_AGE -> repository.findByEmployeeAge(
                    validateAndFormatEmployeeAge(readString("employee age (over 17 years old)"))
            );

            case EMPLOYEE_HIRE_DATE -> repository.findByEmployeeHireDate(validadeAndFormatDate(
                            readString("update time (pattern DD/MM/YYYY with symbols)"),
                            "dd/MM/uuuu",
                            LocalDate::from
                    )
            );

            case OUT -> Optional.empty();
        };

        if (option == DepartmentFind.OUT) {
            log.info("Operation cancelled!");
            return null;
        }

        return department.orElseThrow(() -> new DepartmentException("Department not found!"));
    }

    public Department findAndDelete(final String name) {

        try {
            return repository.findAndDelete(name)
                    .orElseThrow(() -> new DepartmentException("Department not found!"));

        } catch (Exception e) {
            throw new DatabaseException(format("Error ocurred in find and delete: %s", e.getMessage()), e.getCause());
        }

    }

    public String validateAndFormatName(final String name) {

        if (name.length() < 3) throw new DepartmentException("Short name!");

        if (name.matches("^[A-Za-zÀ-ÖØ-öø-ÿ]+$"))
            throw new DepartmentException(format("%s contains special symbol!", name));

        return FormatterUtils.formatName(name);
    }

    public Integer validateAndFormatEmployeeAge(final String value) {

        try {
            int employeeAge = Integer.parseInt(value);
            if (employeeAge > 17) return employeeAge;

            log.error("Invalid age, under 18 years old!");

        } catch (NumberFormatException e) {
            log.error("Invalid age {}! Only numbers", value);
        }

        return null;
    }

    public <T extends TemporalAccessor> T validadeAndFormatDate(final String value, final String pattern, final TemporalQuery<T> query) {

        try {
            return FormatterUtils.formatStringToTemporal(value, pattern, query);

        } catch (DateTimeParseException e) {
            throw new DepartmentException(format("%s is invalid! Pattern required: %s", value, pattern), e);
        }

    }

    //Possibility of adding new options
    public DepartmentResponse updateByOption(final DepartmentUpdate option, final Department department) {

        final String newName = validateAndFormatName(
                readString("department name (without special characters and more than 2 characters)")
        );

        try {
            repository.updateName(department, newName);
            return mapper.departmentToResponse(department);

        } catch (ConstraintViolationException e) {
            throw new DatabaseException(format("Name %s already exists!", newName), e);

        } catch (Exception e) {
            throw new DatabaseException(format("Error occured in update department: %s", e.getMessage()), e);
        }

    }

}
