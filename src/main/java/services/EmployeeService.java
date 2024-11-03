package services;


import criteria.EmployeeFilter;
import dtos.response.EmployeeResponse;
import enums.employee.EmployeeFind;
import enums.employee.EmployeeUpdate;
import enums.menu.YesOrNo;
import exceptions.EmployeeException;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import mappers.EmployeeMapper;
import model.*;
import org.hibernate.exception.ConstraintViolationException;
import repositories.interfaces.EmployeeRepository;
import utils.FormatterUtils;
import utils.ReaderUtils;

import java.math.BigDecimal;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalQuery;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static utils.ReaderUtils.*;

@Log4j2
public final class EmployeeService {

    private final EmployeeRepository repository;

    @Getter
    private final EmployeeMapper mapper;

    public EmployeeService(EmployeeRepository repository, EmployeeMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public EmployeeResponse save(final Employee employee) {

        try {
            repository.save(employee);
            return mapper.employeeToResponse(employee);

            //If document already exists
        } catch (ConstraintViolationException e) {
            throw new EmployeeException("Employee already exists!", e);

        } catch (Exception e) {
            throw new EmployeeException(format("Error in save: %s", e.getMessage()), e);
        }
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
                jobs.add(Job.builder()
                        .department(department)
                        .employee(employee)
                        .level(level)
                        .salary(salary)
                        .build());


                //Removing department of hiring
                departments.remove(department);

                yesOrNo = readEnum("Do you want hire this employee for other departments? ", YesOrNo.class);

            } catch (InputMismatchException e) {
                log.error("Invalid entry!");
            }

        } while (yesOrNo != YesOrNo.NO);

        return jobs;
    }


    public void defineSpecificAtributtes(final Employee employee) {

        try {

            if (employee instanceof NormalEmployee ne) {

                YesOrNo yesOrNo = readEnum(
                        format("Employee %s has faculty?", ne.getName()),
                        YesOrNo.class
                );

                if (yesOrNo == YesOrNo.YES) ne.setHasFaculty();

            } else if (employee instanceof SuperiorEmployee se) {

                validateAndDefineWorkExperience(
                        se,
                        se.getAge(),
                        readInt("valid work experience (more than one year and less than the employee age)")
                );

            }

        } catch (Exception e) {
            log.error("{} - Default info is defined!", e.getMessage());
            //Error occured = Has faculty false or work experience is 1
        }

    }

    public void validateAndDefineWorkExperience(final SuperiorEmployee se, final int age, final int workExperience) {

        if (workExperience < 1) throw new EmployeeException("Should be has work experience!");

        if (workExperience > age) throw new EmployeeException("Did the employee work before they were born?");

        se.setWorkExperience(workExperience);
    }

    public Set<EmployeeResponse> findByFilters(final EmployeeFilter filters) {

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
                        if (!departmentName.equalsIgnoreCase(filters.getDepartmentName())) {
                            filters.setDepartmentName(departmentName.toLowerCase());
                        }

                    }
                    case EMPLOYEE_NAME -> {

                        final String employeeName = readString("employee name");
                        if (!employeeName.equalsIgnoreCase(filters.getEmployeeName())) {
                            filters.setEmployeeName(employeeName.toUpperCase());
                        }

                    }
                    case DOCUMENT -> {

                        final String document = validateAndFormatDocument(readString("CPF (patern xxx.xxx.xxx-xx with symbols)"));
                        if (!document.equals(filters.getDocument())) {
                            filters.setDocument(document);
                        }

                    }
                    case AGE -> {

                        final int employeeAge = readInt("age");
                        if (employeeAge != filters.getEmployeeAge()) {
                            filters.setEmployeeAge(employeeAge);
                        }

                    }
                    case WORK_EXPERIENCE -> {

                        final int workExperience = readInt("work experience");
                        if (workExperience != filters.getWorkExperience()) {
                            filters.setWorkExperience(workExperience);
                        }

                    }
                    case BIRTH_DATE -> {

                        final LocalDate birthDate = parseAndValidateTemporal(
                                readString("birth date (pattern DD/MM/YYYY)"),
                                LocalDate.class,
                                LocalDate::from
                        );

                        if (!birthDate.isEqual(filters.getBirthDate())) {
                            filters.setBirthDate(birthDate);
                        }

                    }
                    case HIRE_DATE -> {

                        final LocalDate hireDate = parseAndValidateTemporal(
                                readString("hire date (pattern DD/MM/YYYY)"),
                                LocalDate.class,
                                LocalDate::from
                        );

                        if (!hireDate.isEqual(filters.getHireDate())) {
                            filters.setHireDate(hireDate);
                        }

                    }
                    case HIRE_TIME -> {

                        final LocalTime hireTime = parseAndValidateTemporal(
                                readString("hire time (pattern HH:MM)"),
                                LocalTime.class,
                                LocalTime::from
                        );

                        if (!hireTime.equals(filters.getHireTime())) {
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


    public Employee findByDocument(final String document) {
        return repository.findByDocument(document)
                .orElseThrow(() -> new EmployeeException(format("Employee with document %s not found!", document)));
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

        if (yesOrNo == YesOrNo.NO) {
            log.info("Dismissal cancelled!");
            return;
        }

        try {
            repository.delete(employee);
        } catch (Exception e) {
            throw new EmployeeException(format("Error occured in delete employee: %s", e.getMessage()), e);
        }

        log.info("Employee dismissed!");
    }


    public BigDecimal validateAndFormatSalary(final String value) {

        if (!value.matches("^[0-9]+([.,][0-9]{1,2})?$")) {
            throw new EmployeeException(format("%s is invalid salary! (Only numbers and decimal values separated by dot or comma)", value));
        }

        return new BigDecimal(value.replace(",", "."));
    }


    public String validateAndFormatName(final String value) {

        if (value.length() < 3) throw new EmployeeException(format("%s is a short name!", value));

        if (!value.matches("[A-Za-zÀ-ÖØ-öø-ÿ]+$")) {
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

        final LocalDate now = LocalDate.now();
        final Period diff = birthDate.until(now);

        if (diff.getYears() < 15) throw new EmployeeException("Employee underage!");

        boolean itsAnniversary = diff.getMonths() == 0 && diff.getDays() == 0;
        boolean aniversaryWasBefore = diff.getMonths() == 0 && birthDate.getDayOfMonth() < now.getDayOfMonth();

        return (itsAnniversary || aniversaryWasBefore)
                ? diff.getYears()
                : diff.getYears() - 1;
    }

    public <T extends Temporal> T parseAndValidateTemporal(final String value, final Class<T> temporalClass, final TemporalQuery<T> query) {

        try {
            return FormatterUtils.formatStringToTemporal(value, temporalClass, query);

        } catch (DateTimeException e) {
            throw new EmployeeException(format("%s is invalid date/time", value), e);
        }

    }

}
