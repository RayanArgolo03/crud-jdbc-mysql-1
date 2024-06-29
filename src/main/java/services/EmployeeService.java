package services;


import domain.department.Department;
import domain.department.Level;
import domain.employee.Employee;
import domain.employee.NormalEmployee;
import domain.employee.SuperiorEmployee;
import dto.employee.EmployeeBaseDTO;
import dto.employee.NormalEmployeeDTO;
import dto.employee.SuperiorEmployeeDTO;
import enums.employee.EmployeeDeleteOption;
import enums.employee.EmployeeFindOption;
import enums.employee.EmployeeType;
import enums.employee.EmployeeUpdateOption;
import enums.menu.YesOrNo;
import exceptions.DbConnectionException;
import exceptions.EmployeeException;
import factory.EmployeeBuilderFactory;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import mappers.interfaces.Mapper;
import repositories.interfaces.EmployeeRepository;
import utils.EnumListUtils;
import utils.FormatterUtils;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

import static utils.ReaderUtils.*;

@FieldDefaults(makeFinal = true)
@AllArgsConstructor
public final class EmployeeService {

    private Mapper<NormalEmployeeDTO, NormalEmployee> normalMapper;
    private Mapper<SuperiorEmployeeDTO, SuperiorEmployee> superiorMapper;
    private EmployeeRepository repository;

    public String validateAndFormatName(final String name) {

        Objects.requireNonNull(name, "Name can´t be null!");

        if (name.length() < 3) throw new EmployeeException(String.format("%s is a small name!", name));

        if (!name.matches("^[A-Za-z]+((\\s)?((['\\-.])?([A-Za-z])+))*$")) {
            throw new EmployeeException(String.format("%s contains special characters!", name));
        }

        return FormatterUtils.formatName(name);

    }

    public void validateDocument(final String document) {

        Objects.requireNonNull(document, "Document can´t be null!");

        if (!document.matches("(^\\d{3}\\x2E\\d{3}\\x2E\\d{3}\\x2D\\d{2}$)")) {
            throw new EmployeeException(String.format("Invalid document: %s does not match the pattern!", document));
        }

    }

    public LocalDate parseAndValidateDate(final String dateInString) {
        Objects.requireNonNull(dateInString, "Birth date can´t be null");
        try {
            return LocalDate.parse(dateInString, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (DateTimeParseException e) {
            throw new EmployeeException(String.format("Invalid date! %s does not match the pattern dd/MM/yyyy!", dateInString), e);
        }
    }

    public BigDecimal validateAndFormatSalary(final String salaryInString) {

        Objects.requireNonNull(salaryInString, "Salary can´t be null!");

        if (!salaryInString.matches("^[0-9]+([.,][0-9]{1,2})?$")) {
            throw new EmployeeException(String.format("%s does not match the pattern!", salaryInString));
        }

        return new BigDecimal(FormatterUtils.formatMoney(salaryInString));
    }


    public int generateAge(final LocalDate birthDate) {

        final Period p = birthDate.until(LocalDate.now());

        final int age = (p.getMonths() == 0 && p.getDays() == 0)
                ? p.getYears() //It´s aniversary
                : p.getYears() - 1;

        if (age < 18) throw new EmployeeException("Employee has less than eighteen years!");

        return age;
    }

    public Map<Department, Map<Level, BigDecimal>> receiveJobsInformation(final List<Department> departments) {

        final Map<Department, Map<Level, BigDecimal>> dls = new HashMap<>();

        YesOrNo yesOrNo;
        do {
            if (departments.isEmpty()) {
                System.out.println("There are no more departments to allocate!");
                break;
            }

            Department department = readElement("department option", departments);

            Level level = readElement("level option", EnumListUtils.getEnumList(Level.class));

            String salaryInString = readString("employee salary (only numbers and decimal values separated by dot or comma");
            BigDecimal salary = validateAndFormatSalary(salaryInString);
            System.out.printf("\n Salary: %s\n", NumberFormat.getCurrencyInstance().format(salary));

            //Hire action
            dls.put(department, Map.of(level, salary));

            //Removing department of hiring
            departments.remove(department);

            yesOrNo = readElement("Do you want hire this employee for other departments? ",
                    EnumListUtils.getEnumList(YesOrNo.class));

        } while (yesOrNo != YesOrNo.NO);

        return dls;
    }

    public Employee createEmployee(final String name, final String document,
                                   final LocalDate birthDate,
                                   final int age,
                                   final Map<Department, Map<Level, BigDecimal>> dls,
                                   final EmployeeType type) {

        Objects.requireNonNull(type, "Employee type can´t be null!");

        return EmployeeBuilderFactory.newEmployeeBuilder(type)
                .name(name)
                .document(document)
                .birthDate(birthDate)
                .age(age)
                .departmentsAndLevelsAndSalaries(dls)
                .build();
    }

    public void defineSpecificAtributtes(final Employee employee) {

        if (employee instanceof NormalEmployee ne) {
            String title = String.format("Employee %s has faculty?", ne.getName());
            defineHasFaculty(ne, readElement(title, EnumListUtils.getEnumList(YesOrNo.class)));
        } else if (employee instanceof SuperiorEmployee se) {
            defineWorkExperience(se, se.getAge(), readInt("Valid Work experience (more than one year and less than the employee age)"));
        }
    }

    public void defineHasFaculty(final NormalEmployee ne, final YesOrNo option) {
        if (option == YesOrNo.YES) ne.setHasFaculty(true);
    }

    public void defineWorkExperience(final SuperiorEmployee se, final int age, final int workExperience) {

        if (workExperience < 1) throw new EmployeeException("Should be has work experience!");

        if (workExperience > age) throw new EmployeeException("Did the employee work before they were born?");

        if (age - workExperience < 15) {
            throw new EmployeeException("Superior Employee can´t be started work under the age of fifteen! (15 years old)");
        }

        se.setWorkExperience(workExperience);
    }

    public void saveBaseEmployee(final Employee employee) {
        try {
            repository.save(employee);
        } catch (DbConnectionException e) {
            throw new EmployeeException(String.format("Error in save: %s", e.getMessage()), e);
        }
    }

    public void saveSpecificEmployee(final Employee employee) {
        if (employee instanceof NormalEmployee ne) repository.saveNormalEmployee(ne);
        else if (employee instanceof SuperiorEmployee se) repository.saveSuperiorEmployee(se);
    }


    public boolean validSalaryToUpdate(final BigDecimal oldSalary, final BigDecimal newSalary) {
        return oldSalary.compareTo(newSalary) != 0;
    }

    public List<Employee> findByOption(final EmployeeFindOption option) {

        return switch (option) {
            case ID -> {
                final long employeeId = readLong("employee id");
                yield findById(employeeId);
            }
            case NAME -> {
                final String name = readString("name");
                yield findByName(name);
            }
            case DOCUMENT -> {
                final String document = readString("document");
                yield findByDocument(document);
            }
            case AGE -> {
                final int age = readInt("age");
                yield findByAge(age);
            }
            case HIRE_DATE -> {
                final LocalDate hireDateWithoutTime = parseAndValidateDate(
                        readString("hire date")
                );
                yield findByHireDate(hireDateWithoutTime);
            }
        };
    }

    public List<Employee> findById(final long employeeId) {
        return Collections.singletonList(repository.findById(employeeId)
                .map(this::mappperToSpecificEntity)
                .orElseThrow(() -> new EmployeeException(String.format("Employee with id %d not found!", employeeId))));
    }

    public List<Employee> findByName(final String name) {

        Objects.requireNonNull(name, "Name can´t be null");

        final List<EmployeeBaseDTO> list = repository.findByName(name);
        if (list.isEmpty()) throw new EmployeeException(String.format("Employees not found by name %s!", name));

        return list.stream()
                .map(this::mappperToSpecificEntity)
                .collect(Collectors.toList());
    }

    public List<Employee> findByDocument(final String document) {

        Objects.requireNonNull(document, "Document can´t be null!");

        return Collections.singletonList(repository.findByDocument(document)
                .map(this::mappperToSpecificEntity)
                .orElseThrow(() -> new EmployeeException(String.format("Employee not found by document %s!", document))));
    }

    public List<Employee> findByAge(final int age) {

        final List<EmployeeBaseDTO> list = repository.findByAge(age);
        if (list.isEmpty()) throw new EmployeeException(String.format("Employees not found by age %d!", age));

        return list.stream()
                .map(this::mappperToSpecificEntity)
                .collect(Collectors.toList());
    }

    public List<Employee> findByHireDate(final LocalDate hireDateWithoutTime) {

        final List<EmployeeBaseDTO> list = repository.findByHireDate(hireDateWithoutTime);

        if (list.isEmpty()) {
            throw new EmployeeException(String.format("Employees not found by hire date %s!", DateTimeFormatter.ofPattern("dd/MM/yyyy").format(hireDateWithoutTime)));
        }

        return list.stream()
                .map(this::mappperToSpecificEntity)
                .collect(Collectors.toList());
    }

    public void updateByOption(final EmployeeUpdateOption option, final Employee employee) {

        switch (option) {
            case NAME -> {
                final String newName = readString("new name");
                updateName(employee, newName);
            }
            case DOCUMENT -> {
                final String newDocument = readString("new document");
                updateDocument(employee, newDocument);
            }
            case SENIORITY_OF_WORK -> {

                final Department department = readElement(
                        "Choose the department and level you want!",
                        new ArrayList<>(employee.getDepartmentsAndLevelsAndSalaries().keySet())
                );

                final Level oldLevel = new ArrayList<>(employee.getDepartmentsAndLevelsAndSalaries().get(department).keySet()).get(0);

                final List<Level> levelsWithoutOld = EnumListUtils.getEnumList(Level.class);
                levelsWithoutOld.remove(oldLevel);

                final Level newLevel = readElement("New level!", levelsWithoutOld);

                updateLevel(employee, department, newLevel, oldLevel);
            }
            case SALARY_OF_WORK -> {

                final Department department = readElement(
                        "Choose the department and salary you want!",
                        new ArrayList<>(employee.getDepartmentsAndLevelsAndSalaries().keySet())
                );

                final BigDecimal oldSalary = employee.getDepartmentsAndLevelsAndSalaries()
                        .get(department)
                        .values().stream()
                        .findFirst()
                        .get();

                //Validate null salary here
                final String salaryInString = readString(String.format("new salary (different from the current salary, %s)", oldSalary));
                final BigDecimal newSalary = validateAndFormatSalary(salaryInString);

                updateSalary(employee, department, newSalary, oldSalary);
            }
        }
    }

    public void updateName(final Employee employee, final String newName) {

        Objects.requireNonNull(newName, "New name can´t be null!");

        if (newName.equals(employee.getName())) {
            throw new EmployeeException(String.format("Name %s can´t be equals to current name!", newName));
        }

        repository.updateName(employee, newName);
        System.out.println("Name updated!");
    }

    public void updateDocument(final Employee employee, final String newDocument) {

        Objects.requireNonNull(newDocument, "New document can´t be null!");

        if (newDocument.equals(employee.getDocument())) {
            throw new EmployeeException(String.format("Document %s can´t be equals to current document!", newDocument));
        }

        repository.updateDocument(employee, newDocument);
        System.out.println("Document updated!");
    }

    public void updateLevel(final Employee employee, final Department department, final Level newLevel, final Level oldLevel) {
        Objects.requireNonNull(newLevel, "New level can´t be null!");
        repository.updateLevel(employee, department, newLevel, oldLevel);
    }

    public void updateSalary(final Employee employee, final Department department, final BigDecimal newSalary, final BigDecimal oldSalary) {
        if (oldSalary.equals(newSalary)) throw new EmployeeException("Salary can´t be equals to current salary!");
        repository.updateSalary(employee, department, newSalary, oldSalary);
    }


    public int deleteByOption(final EmployeeDeleteOption option, final List<Department> departments) {

        return switch (option) {
            case ID -> {
                final long id = readLong("id");
                yield deleteById(id);
            }
            case NAME -> {
                final String name = readString("name");
                yield deleteByName(name);
            }
            case DOCUMENT -> {
                final String document = readString("document");
                yield deleteByDocument(document);
            }
            case HIRE_DATE -> {
                final LocalDate hireDateWithoutTime = parseAndValidateDate(
                        readString("hire date")
                );
                yield deleteByHireDate(hireDateWithoutTime);
            }
            case DEPARTMENT -> {
                final Department department = readElement("department", departments);
                yield deleteByDepartment(department);
            }
        };
    }

   //Todo continue
    public int deleteById(final long id) {
        try {
            return repository.deleteById(id);
        } catch (DbConnectionException e) {
            throw new EmployeeException(String.format("Error: %s", e.getMessage()), e);
        }
    }

    public int deleteByName(final String name) {
        try {
            return repository.deleteByName(name);
        } catch (DbConnectionException e) {
            throw new EmployeeException(String.format("Error: %s", e.getMessage()), e);
        }
    }

    public int deleteByDocument(final String document) {
        try {
            return repository.deleteByDocument(document);
        } catch (DbConnectionException e) {
            throw new EmployeeException(String.format("Error: %s", e.getMessage()), e);
        }
    }

    public int deleteByHireDate(final LocalDate hireDateWithoutTime) {
        try {
            return repository.deleteByHireDate(hireDateWithoutTime);
        } catch (DbConnectionException e) {
            throw new EmployeeException(String.format("Error: %s", e.getMessage()), e);
        }
    }

    public int deleteByDepartment(final Department department) {
        try {
            return repository.deleteByDepartment(department);
        } catch (DbConnectionException e) {
            throw new EmployeeException(String.format("Error: %s", e.getMessage()), e);
        }
    }

    private Employee mappperToSpecificEntity(final EmployeeBaseDTO dto) {

        if (dto instanceof SuperiorEmployeeDTO sed) {
            return superiorMapper.dtoToEntity(sed);
        } else if (dto instanceof NormalEmployeeDTO ned) {
            return normalMapper.dtoToEntity(ned);
        }

        throw new EmployeeException("Invalid type");
    }
}
