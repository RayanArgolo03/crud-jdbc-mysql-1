package services;

import criteria.DepartmentFilter;
import dtos.response.DepartmentResponse;
import enums.department.DepartmentFind;
import enums.department.DepartmentUpdate;
import exceptions.DatabaseException;
import exceptions.DepartmentException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
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
import java.time.temporal.Temporal;
import java.time.temporal.TemporalQuery;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static utils.ReaderUtils.readEnum;
import static utils.ReaderUtils.readString;

@Log4j2
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public final class DepartmentService {

    DepartmentRepository repository;

    @Getter
    DepartmentMapper mapper;

    public List<Department> findAll() {

        final List<Department> list = repository.findAll();
        if (list.isEmpty()) throw new DepartmentException("No departments in the database!");

        return list;
    }

    public DepartmentResponse save(final Department department) {

        try {
            repository.save(department);

            return mapper.departmentToResponse(department);

        } catch (ConstraintViolationException e) {
            throw new DepartmentException(format("Department %s already exists!", department.getName()), e);

        } catch (Exception e) {
            throw new DepartmentException(format("Error occured on create department: %s", e.getMessage()), e.getCause());
        }

    }

    public Set<DepartmentResponse> findByFilters(final DepartmentFilter filters) {

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

                        final LocalDate createdDate = validateAndFormatDate(
                                readString("creation date (pattern DD/MM/YYYY with bars symbols)"),
                                LocalDate.class,
                                LocalDate::from
                        );
                        if (!createdDate.isEqual(filters.getCreationDate())) {
                            filters.setCreationDate(createdDate);
                        }

                    }
                    case UPDATE_DATE -> {

                        final LocalDateTime updateDate = validateAndFormatDate(
                                readString("creation date (pattern DD/MM/YYYY HH:MM with symbols)"),
                                LocalDateTime.class,
                                LocalDateTime::from
                        );
                        //Removing miliseconds to comparison in database
                        if (!updateDate.isEqual(filters.getLastUpdateDate())) {
                            filters.setLastUpdateDate(updateDate.truncatedTo(ChronoUnit.MICROS));
                        }
                    }
                    case UPDATE_TIME -> {

                        final LocalTime updateTime = validateAndFormatDate(
                                readString("update time (pattern HH:MM with symbol)"),
                                LocalTime.class,
                                LocalTime::from
                        );
                        if (!updateTime.equals(filters.getLastUpdateTime())) {
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
                        if (!employeeAge.equals(filters.getEmployeeAge())) {
                            filters.setEmployeeAge(employeeAge);
                        }

                    }
                    case EMPLOYEE_HIRE_DATE -> {

                        final LocalDate employeeHireDate = validateAndFormatDate(
                                readString("update time (pattern DD/MM/YYYY with symbols)"),
                                LocalDate.class,
                                LocalDate::from
                        );
                        if (!employeeHireDate.equals(filters.getEmployeeHireDate())) {
                            filters.setEmployeeHireDate(employeeHireDate);
                        }

                    }
                }

            } catch (InputMismatchException e) {
                log.error("Invalid value!");

            } catch (Exception e) {
                log.error("Error: {}", e.getMessage());
            }

        } while (option != DepartmentFind.OUT);

        if (!filters.hasFilters()) return null;

        return filters;
    }

    public Department findByOption(final DepartmentFind option) {

        final Optional<Department> department = switch (option) {

            case DEPARTMENT_NAME -> repository.findByDepartmentName(
                    readString("department name").toUpperCase()
            );

            case CREATION_DATE -> repository.findByCreationDate(
                    validateAndFormatDate(
                            readString("creation date (pattern DD/MM/YYYY with bars symbols)"),
                            LocalDate.class,
                            LocalDate::from
                    )
            );

            //Removing miliseconds to comparison in database
            case UPDATE_DATE -> repository.findByUpdateDate(
                    validateAndFormatDate(
                            readString("creation date (pattern DD/MM/YYYY HH:MM with symbols)"),
                            LocalDateTime.class,
                            LocalDateTime::from
                    ).truncatedTo(ChronoUnit.MICROS)

            );

            case UPDATE_TIME -> repository.findByUpdateTime(
                    validateAndFormatDate(
                            readString("update time (pattern HH:MM with symbol)"),
                            LocalTime.class,
                            LocalTime::from
                    )
            );

            case EMPLOYEE_NAME -> repository.findByEmployeeName(readString("employee name").toUpperCase());

            case EMPLOYEE_AGE -> repository.findByEmployeeAge(
                    validateAndFormatEmployeeAge(
                            readString("employee age (over 17 years old)")
                    )
            );

            case EMPLOYEE_HIRE_DATE -> repository.findByEmployeeHireDate(
                    validateAndFormatDate(
                            readString("update time (pattern DD/MM/YYYY with symbols)"),
                            LocalDate.class,
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

    public DepartmentResponse findAndDelete(final String name) {

        Optional<Department> department;
        try {
            department = repository.findAndDelete(name);

        } catch (Exception e) {
            throw new DatabaseException(format("Error ocurred in find and delete: %s", e.getMessage()), e.getCause());
        }

        return department
                .map(mapper::departmentToResponse)
                .orElseThrow(() -> new DepartmentException(format("Department %s not found!", name)));
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
            throw new DepartmentException(format("Name %s already exists!", newName), e);

        } catch (Exception e) {
            throw new DatabaseException(format("Error occured in update department: %s", e.getMessage()), e);
        }

    }

    public String validateAndFormatName(final String name) {

        if (name.length() < 3) throw new DepartmentException(format("%s is a short name!", name));

        if (!name.matches("[A-Za-zÀ-ÖØ-öø-ÿ]+$"))
            throw new DepartmentException(format("%s contains special symbol!", name));

        return FormatterUtils.formatName(name);
    }

    public Integer validateAndFormatEmployeeAge(final String temp) {

        try {
            int employeeAge = Integer.parseInt(temp);

            if (employeeAge < 18) throw new DepartmentException("Invalid age, under 18 years old!");

            return employeeAge;

        } catch (NumberFormatException e) {
            throw new DepartmentException(format("%s is a invalid age! Only numbers", temp), e);
        }

    }

    public <T extends Temporal> T validateAndFormatDate(final String value, final Class<T> temporalClass, final TemporalQuery<T> query) {

        try {
            return FormatterUtils.formatStringToTemporal(value, temporalClass, query);

        } catch (DateTimeParseException e) {
            throw new DepartmentException(format("%s is a invalid date/time!", value), e);
        }

    }



}
