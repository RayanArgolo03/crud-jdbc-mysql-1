package services;


import criteria.EmployeeFilter;
import dtos.response.EmployeeResponse;
import enums.employee.EmployeeFind;
import enums.employee.EmployeeUpdate;
import enums.menu.YesOrNo;
import exceptions.DatabaseException;
import exceptions.EmployeeException;
import lombok.extern.log4j.Log4j2;
import mappers.EmployeeMapper;
import model.*;
import org.hibernate.exception.ConstraintViolationException;
import repositories.interfaces.EmployeeRepository;
import utils.FormatterUtils;
import utils.ReaderUtils;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.text.NumberFormat;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQuery;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static utils.ReaderUtils.*;

@Log4j2
public final class EmployeeService {

    private final EmployeeRepository repository;
    private final EmployeeMapper mapper;

    public EmployeeService(EmployeeRepository repository, EmployeeMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public EmployeeMapper getMapper() {
        return mapper;
    }

    public String validateAndFormatName(final String value) {

        if (value.length() < 3) throw new EmployeeException("Short name!");

        if (!value.matches("^[A-Za-zÀ-ÖØ-öø-ÿ]+$")) {
            throw new EmployeeException(format("%s contains special characters!", value));
        }

        return FormatterUtils.formatName(value);

    }

    public String validateAndFormatDocument(final String value) {

        if (!value.matches("(^\\d{3}\\x2E\\d{3}\\x2E\\d{3}\\x2D\\d{2}$)")) {
            throw new EmployeeException(format("CPF %s does not match the patern xxx.xxx.xxx-xx with symbols!", value));
        }

        return value.replaceAll("[^0-9]", "");

    }


    public int generateAge(final LocalDate birthDate) {

        final Period p = birthDate.until(LocalDate.now());

        return (p.getMonths() == 0 && p.getDays() == 0)
                ? p.getYears() //It´s aniversary
                : p.getYears() - 1;
    }

    public Set<Job> createJobs(final List<Department> departments, final Employee employee) {

        final Set<Job> jobs = new HashSet<>();

        YesOrNo yesOrNo = null;
        do {

            if (departments.isEmpty()) {
                log.info("There are no more departments to allocate, stopping..");
                break;
            }

            try {

                Department department = readElement("department option", departments);
                Level level = readEnum("level option", Level.class);

                BigDecimal salary = validateAndFormatSalary(
                        ReaderUtils.readString("employee salary (only numbers and decimal values separated by dot or comma")
                );

                System.out.printf("\n Salary: %s\n", FormatterUtils.formatSalary(salary));

                //Hire action
                jobs.add(new Job(department, employee, level, salary));

                //Removing department of hiring
                departments.remove(department);

                yesOrNo = readEnum("Do you want hire this employee for other departments? ", YesOrNo.class);

            } catch (InputMismatchException e) {
                log.error("Invalid entry!");
            }

        } while (yesOrNo != YesOrNo.NO);

        return jobs;
    }

    public BigDecimal validateAndFormatSalary(final String value) {

        if (!value.matches("^[0-9]+([.,][0-9]{1,2})?$")) {
            throw new EmployeeException(format("%s is invalid salary!", value));
        }

        return new BigDecimal(value.replace(",", "."));
    }

    public void defineSpecificAtributtes(final Employee employee) {

        try {

            if (employee instanceof NormalEmployee ne) {

                YesOrNo yesOrNo = readEnum(
                        format("Employee %s has faculty?", ne.getName()),
                        YesOrNo.class
                );

                if (yesOrNo == YesOrNo.YES) ne.hasFaculty();

            } else if (employee instanceof SuperiorEmployee se) {

                validateAndDefineWorkExperience(
                        se,
                        se.getAge(),
                        readInt("valid work experience (more than one year and less than the employee age)")
                );

            }

        } catch (Exception e) {
            log.error("{} - Default info is defined!", e.getMessage());
            //Error occured = Has faculty null or work experience is 1
        }

    }

    public void validateAndDefineWorkExperience(final SuperiorEmployee se, final int age, final int workExperience) {

        if (workExperience < 1) throw new EmployeeException("Should be has work experience!");

        if (workExperience > age) throw new EmployeeException("Did the employee work before they were born?");

        if (age - workExperience < 15) {
            throw new EmployeeException("Can´t be started work under the age of fifteen! (15 years old)");
        }

        se.setWorkExperience(workExperience);
    }

    public EmployeeResponse saveBaseEmployee(final Employee employee) {

        try {
            repository.save(employee);
            return mapper.employeeToResponse(employee);

            //If document already exists
        } catch (ConstraintViolationException e) {
            throw new EmployeeException("Employee already exists!", e);

        } catch (DatabaseException e) {
            throw new EmployeeException(format("Error in save: %s", e.getMessage()), e);
        }
    }


    public Set<EmployeeResponse> findByFilters() {

        final EmployeeFilter filters = createFilters();
        Objects.requireNonNull(filters, "No filters to find employees!");

        final Set<Employee> employees = repository.findByFilters(filters);
        if (employees.isEmpty()) throw new EmployeeException("Employees not found by filters!");

        return employees.stream()
                .map(mapper::employeeToResponse)
                .collect(Collectors.toSet());
    }

    public EmployeeFilter createFilters() {

        final EmployeeFilter filters = new EmployeeFilter();

        EmployeeFind option = null;
        do {
            try {

                switch ((option = readEnum("employee filter option", EmployeeFind.class))) {

                    case DEPARTMENT_NAME -> {

                        final String departmentName = readString("department name");
                        if (filters.getDepartmentName() == null || !departmentName.equalsIgnoreCase(filters.getDepartmentName())) {
                            filters.setDepartmentName(departmentName.toLowerCase());
                        }

                    }
                    case EMPLOYEE_NAME -> {

                        final String employeeName = readString("employee name");
                        if (filters.getEmployeeName() == null || !employeeName.equalsIgnoreCase(filters.getEmployeeName())) {
                            filters.setEmployeeName(employeeName.toLowerCase());
                        }

                    }
                    case DOCUMENT -> {

                        final String document = validateAndFormatDocument(readString("CPF (patern xxx.xxx.xxx-xx with symbols)"));
                        if (filters.getDocument() == null || !document.equals(filters.getDocument())) {
                            filters.setDocument(document);
                        }

                    }
                    case AGE -> {

                        final int employeeAge = readInt("age");
                        if (filters.getEmployeeAge() == null || employeeAge != filters.getEmployeeAge()) {
                            filters.setEmployeeAge(employeeAge);
                        }

                    }
                    case WORK_EXPERIENCE -> {

                        final int workExperience = readInt("work experience");
                        if (filters.getWorkExperience() == null || workExperience != filters.getWorkExperience()) {
                            filters.setWorkExperience(workExperience);
                        }

                    }
                    case BIRTH_DATE -> {

                        final LocalDate birthDate = parseAndValidateTemporal(
                                readString("birth date (pattern DD/MM/YYYY)"),
                                "dd/MM/uuuu",
                                LocalDate::from
                        );

                        if (filters.getBirthDate() == null || !filters.getBirthDate().isEqual(birthDate)) {
                            filters.setBirthDate(birthDate);
                        }

                    }
                    case HIRE_DATE -> {

                        final LocalDate hireDate = parseAndValidateTemporal(
                                readString("hire date (pattern DD/MM/YYYY)"),
                                "dd/MM/uuuu",
                                LocalDate::from
                        );

                        if (filters.getHireDate() == null || !filters.getHireDate().isEqual(hireDate)) {
                            filters.setHireDate(hireDate);
                        }

                    }
                    case HIRE_TIME -> {

                        final LocalTime hireTime = parseAndValidateTemporal(
                                readString("hire time (pattern HH:MM)"),
                                "HH:mm",
                                LocalTime::from
                        );

                        if (filters.getHireTime() == null || !filters.getHireTime().equals(hireTime)) {
                            filters.setHireTime(hireTime);
                        }

                    }

                    case HAS_FACULTY -> filters.setHasFaculty(readEnum("employee has faculty? ", YesOrNo.class));

                }

            } catch (Exception e) {
                log.error("Invalid value: {}", e.getMessage());
            }

        } while (option != EmployeeFind.OUT);

        if (!filters.hasFilters()) return null;

        return filters;
    }

    public <T extends TemporalAccessor> T parseAndValidateTemporal(final String value, final String pattern, final TemporalQuery<T> query) {

        try {
            return FormatterUtils.formatStringToTemporal(value, pattern, query);

        } catch (DateTimeException e) {
            throw new EmployeeException(format("%s does not matches the pattern %s!", value, pattern), e);
        }

    }

    public Employee findByName(final String name) {
        return repository.findByName(name)
                .orElseThrow(() -> new EmployeeException(format("Employee %s not found!", name)));
    }

    public EmployeeResponse updateByOption(final Employee employee) {

        EmployeeUpdate option = null;
        do {
            try {

                switch ((option = readEnum("update option", EmployeeUpdate.class))) {

                    case NAME -> {

                        final String newName = validateAndFormatName(readString("first name (without special characters and more than three letters!)"));
                        if (!newName.equalsIgnoreCase(employee.getName())) {
                            employee.setName(newName);
                            log.info("Name updated!");
                        }
                    }
                    case DOCUMENT -> {

                        final String newDocument = readString("CPF (patern xxx.xxx.xxx-xx with symbols)");
                        if (!newDocument.equals(employee.getDocument())) {
                            employee.setDocument(newDocument);
                            log.info("Document updated!");
                        }

                    }
                    case SENIORITY_OF_WORK, SALARY_OF_WORK -> {

                        final List<Department> employeeDepartments = employee.getJobs().stream()
                                .map(Job::getDepartment)
                                .collect(Collectors.toList());

                        final Department department = readElement("department you want", employeeDepartments);

                        //Job always exists, Optional get is safe
                        final Job job = employee.getJobs().stream()
                                .filter(j -> j.getDepartment().equals(department))
                                .findFirst()
                                .get();

                        if (option == EmployeeUpdate.SENIORITY_OF_WORK) {

                            final Level oldLevel = job.getLevel();

                            //Instances Modifiable list to remove old level
                            final List<Level> levelsWithoutOld = new ArrayList<>(
                                    Arrays.asList(Level.values())
                            );

                            levelsWithoutOld.remove(oldLevel);

                            final Level newLevel = readElement("new level", levelsWithoutOld);

                            //Memory reference is the same to employee job in Set<Jobs>
                            job.setLevel(newLevel);
                            log.info("Level updated!");

                        } else {

                            final BigDecimal oldSalary = job.getSalary();

                            final BigDecimal newSalary = validateAndFormatSalary(
                                    ReaderUtils.readString("employee salary (only numbers and decimal values separated by dot or comma")
                            );

                            if (!newSalary.equals(oldSalary)) {
                                job.setSalary(newSalary);
                                log.info("Salary updated!");
                            }
                        }

                    }
                }

            } catch (Exception e) {
                log.error("Invalid value!");
            }

        } while (option != EmployeeUpdate.OUT);

        repository.update(employee);
        return mapper.employeeToResponse(employee);
    }


    public void delete(final Employee employee) {

        final YesOrNo yesOrNo = readEnum(format("Do you really want to dismiss %s?", employee.getName()), YesOrNo.class);

        if (yesOrNo == YesOrNo.YES) {

            try {
                repository.deleteByName(employee.getName());
            } catch (Exception e) {
                throw new EmployeeException(format("Error occured in delete employee: %s", e.getMessage()), e);
            }

            log.info("Employee dismissed!");
        }
    }

}
