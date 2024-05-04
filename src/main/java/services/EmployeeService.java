package services;

import dao.impl.EmployeeDAOImpl;
import dao.interfaces.EmployeeDAO;
import domain.departaments.Departament;
import domain.departaments.Level;
import domain.employees.Employee;
import domain.employees.NormalEmployee;
import domain.employees.SuperiorEmployee;
import dto.employee.EmployeeBaseDTO;
import dto.employee.NormalEmployeeDTO;
import dto.employee.SuperiorEmployeeDTO;
import enums.employee.EmployeeDeleteOption;
import enums.employee.EmployeeFindOption;
import enums.employee.EmployeeType;
import enums.employee.EmployeeUpdateOption;
import enums.menu.DefaultMessage;
import enums.menu.YesOrNo;
import exceptions.DepartamentException;
import exceptions.EmployeeException;
import factory.EmployeeBuilderFactory;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import mappers.NormalEmployeeMapper;
import mappers.SuperiorEmployeeMapper;
import utilities.EnumListUtil;
import utilities.FormatterUtil;
import utilities.ReaderUtil;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

;

@FieldDefaults(makeFinal = true)
@AllArgsConstructor
public final class EmployeeService {

    private NormalEmployeeMapper normalEmployeeMapper;
    private SuperiorEmployeeMapper superiorEmployeeMapper;
    private EmployeeDAO dao;
    public String receiveName() {

        String name;
        while (!this.validName(name = ReaderUtil.readString("first name (without special characters and more than one letter!)"))) {
            System.out.println(DefaultMessage.INVALID.getValue() + " Special characters or less than one letter!");
        }

        name = FormatterUtil.formatName(name);

        return name;
    }

    public boolean validName(final String name) {
        return name.length() > 1 && name
                .matches("^[A-Za-z]+((\\s)?((['\\-.])?([A-Za-z])+))*$");
    }

    //Comparing current and new value in update method
    public boolean validStringToUpdate(final String s1, final String s2) {
        return !s1.equalsIgnoreCase(s2);
    }

    public boolean validSalaryToUpdate(final BigDecimal oldSalary, final BigDecimal newSalary) {
        return oldSalary.compareTo(newSalary) != 0;
    }

    public String receiveDocument() {

        String document;
        while (!this.validDocument(document = ReaderUtil.readString("document (CPF with dots and dash)"))) {
            System.out.println(DefaultMessage.INVALID.getValue() + " CPF without dots or dash!");
        }

        return document;
    }

    public boolean validDocument(final String document) {
        return document.matches("(^\\d{3}\\x2E\\d{3}\\x2E\\d{3}\\x2D\\d{2}$)");
    }


    //Using in methods if needs
    public LocalDate receiveDate(final String title) {

        LocalDate date;
        String dateInString = ReaderUtil.readString(title);

        while (Objects.isNull(date = parseAndValidateDate(dateInString))) {
            System.out.println(DefaultMessage.INVALID.getValue());
            dateInString = ReaderUtil.readString(title);
        }

        return date;
    }

    private LocalDate parseAndValidateDate(final String dateInString) {
        try {
            return LocalDate.parse(dateInString, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public int generateAge(final LocalDate birthDate) {

        final Period p = Period.between(birthDate, LocalDate.now());

        final int age = (p.getMonths() == 0 && p.getDays() == 0)
                ? p.getYears()
                : p.getYears() - 1;

        if (!this.validAge(age)) throw new EmployeeException("Employee has less than eighteen years!");

        return age;
    }

    public boolean validAge(final int age) {
        return age > 17;
    }

    public Map<Departament, Map<Level, BigDecimal>> receiveJobsInformation(final List<Departament> departaments) {

        final Map<Departament, Map<Level, BigDecimal>> dls = new HashMap<>();
        final List<Level> levels = EnumListUtil.getEnumList(Level.class);

        YesOrNo option;
        do {
            if (departaments.isEmpty()) {
                System.out.println("There are no more departaments to allocate!");
                break;
            }

            Departament departament = this.receiveDepartament(departaments);
            Level level = this.receiveOption(levels);
            BigDecimal salary = this.receiveSalary();

            //Hire action
            dls.put(departament, Map.of(level, salary));

            //Removing departament of hiring
            departaments.remove(departament);

            System.out.print("Do you want hire this employee for other departaments? ");
            option = ReaderUtil.readEnum(EnumListUtil.getEnumList(YesOrNo.class));

        } while (option != YesOrNo.NO);

        return dls;
    }

    public Employee createEmployee(final String name, final String document,
                                   final LocalDate birthDate,
                                   final int age,
                                   final Map<Departament, Map<Level, BigDecimal>> dls,
                                   final EmployeeType type) {

        final Employee employee = EmployeeBuilderFactory.newEmployeeBuilder(type)
                .name(name)
                .document(document)
                .birthDate(birthDate)
                .age(age)
                .departamentsAndLevelsAndSalaries(dls)
                .build();

        this.setSpecificAtributtes(employee);

        return employee;
    }

    private void setSpecificAtributtes(final Employee employee) {

        if (employee instanceof NormalEmployee normalEmployee) {
            if (this.heHasFaculty()) normalEmployee.setHasFaculty();

        } else if (employee instanceof SuperiorEmployee superiorEmployee) {
            superiorEmployee.setWorkExperience(this.receiveWorkExperience(employee.getAge()));
        }
    }

    public void saveBaseEmployee(final Employee employee) {
        dao.save(employee);
    }

    public void saveSpecificEmployee(final Employee employee) {
        if (employee instanceof NormalEmployee ne) dao.saveNormalEmployee(ne);
        else if (employee instanceof SuperiorEmployee se) dao.saveSuperiorEmployee(se);
        else throw new EmployeeException("Employee must have type!");
    }

    public List<Employee> findByOption(final EmployeeFindOption option) {

        return switch (option) {
            case ID -> {
                final long employeeId = this.receiveId();
                yield Collections.singletonList(this.findById(employeeId));
            }
            case NAME -> {
                final String name = this.receiveName();
                yield this.findByName(name);
            }
            case AGE -> {
                final int age = this.receiveAge();
                yield this.findByAge(age);
            }
            case DOCUMENT -> {
                final String document = this.receiveDocument();
                yield Collections.singletonList(this.findByDocument(document));
            }
            case HIRE_DATE -> {
                final LocalDate hireDateWithoutTime = this.receiveDate("hire date");
                yield this.findByHireDate(hireDateWithoutTime);
            }
        };
    }

    private long receiveId() {

        System.out.printf("%s %s: \n", DefaultMessage.ENTER_WITH.getValue(), "employee id");
        long id;
        while (!this.isPositive(id = ReaderUtil.readLong())) {
            System.out.println(DefaultMessage.INVALID.getValue());
            System.out.printf("%s %s: \n", DefaultMessage.ENTER_WITH.getValue(), "employee id");
        }

        return id;
    }

    public Employee findById(final long employeeId) {
        return dao.findById(employeeId)
                .map(this::mappperToSpecificEntity)
                .orElseThrow(() -> new EmployeeException("Employee not found!"));
    }

    public List<Employee> findByName(final String name) {

        final List<EmployeeBaseDTO> list = dao.findByName(name);
        if (list.isEmpty()) throw new EmployeeException("Employees not found!");

        return list.stream()
                .map(this::mappperToSpecificEntity)
                .collect(Collectors.toList());
    }

    public Employee findByDocument(final String document) {
        return dao.findByDocument(document)
                .map(this::mappperToSpecificEntity)
                .orElseThrow(() -> new EmployeeException("Employee not found!"));
    }

    public List<Employee> findByHireDate(final LocalDate hireDateWithoutTime) {

        final List<EmployeeBaseDTO> list = dao.findByHireDate(hireDateWithoutTime);
        if (list.isEmpty()) throw new EmployeeException("Employees not found!");

        return list.stream()
                .map(this::mappperToSpecificEntity)
                .collect(Collectors.toList());
    }

    public List<Employee> findByAge(final int age) {

        final List<EmployeeBaseDTO> list = dao.findByAge(age);
        if (list.isEmpty()) throw new EmployeeException("Employees not found!");

        return list.stream()
                .map(this::mappperToSpecificEntity)
                .collect(Collectors.toList());
    }

    private int receiveWorkExperience(final int age) {

        System.out.printf("%s %s: \n", DefaultMessage.ENTER_WITH.getValue(), "work experience");

        int workExperience;
        while (!this.validWorkExperience(workExperience = ReaderUtil.readInt(), age)) {
            System.out.println(DefaultMessage.INVALID.getValue() + " You dont´t start work under the age 16..");
            System.out.printf("%s %s: \n", DefaultMessage.ENTER_WITH.getValue(), "work experience");
        }

        return workExperience;
    }

    public boolean validWorkExperience(final int workExperience, final int age) {
        return this.isPositive(workExperience) && (age - workExperience) >= 16;
    }


    public boolean heHasFaculty() {
        System.out.print("This employe has faculty? ");
        return ReaderUtil.readEnum(EnumListUtil.getEnumList(YesOrNo.class)).equals(YesOrNo.YES);
    }


    private Departament receiveDepartament(final List<Departament> departaments) {

        System.out.printf("%s %s \n", DefaultMessage.ENTER_WITH.getValue(), "your departament option:");
        this.printDepartaments(departaments);

        int option;
        while (!this.validOption(option = ReaderUtil.readInt(), departaments.size())) {
            System.out.println(DefaultMessage.INVALID.getValue());
            this.printDepartaments(departaments);
            System.out.printf("%s %s \n", DefaultMessage.ENTER_WITH.getValue(), "your option:");
        }

        return departaments.get(option);
    }

    private void printDepartaments(final List<Departament> departaments) {
        departaments.forEach(d -> System.out.printf("%d - %s \n", departaments.indexOf(d), d.getName()));
    }

    private Departament receiveDepartament(final Map<Departament, Map<Level, BigDecimal>> dls) {

        System.out.printf("\n%s %s \n", DefaultMessage.ENTER_WITH.getValue(), "departament id of the desired update: ");
        this.printDls(dls);
        System.out.print("Your choice: ");
        long choice = ReaderUtil.readLong();

        return dls.keySet().stream()
                .filter(d -> d.getId().equals(choice))
                .findFirst()
                .orElseThrow(() -> new DepartamentException("Invalid!"));
    }

    //Receive any enum list
    public <T extends Enum<T>> T receiveOption(final List<T> list) {
        return ReaderUtil.readEnum(list);
    }

    //Receive enum class and return enum chosen in the utility class
    public <T extends Enum<T>> T receiveOption(final Class<T> enumClass) {
        return ReaderUtil.readEnum(EnumListUtil.getEnumList(enumClass));
    }

    private void printDls(final Map<Departament, Map<Level, BigDecimal>> dls) {
        for (Map.Entry<Departament, Map<Level, BigDecimal>> map : dls.entrySet()) {
            Departament d = map.getKey();
            Level l = map.getValue().keySet().stream().findFirst().get();
            BigDecimal s = map.getValue().get(l);
            System.out.printf("Departament %sWith Level %s and salary %s \n", d, l, this.printSalary(s));
        }
    }


    private BigDecimal receiveSalary() {

        String salaryInString;
        while (!this.validSalary(salaryInString = ReaderUtil.readString("employee salary"))) {
            System.out.println(DefaultMessage.INVALID.getValue());
        }

        salaryInString = FormatterUtil.formatMoney(salaryInString);

        return new BigDecimal(salaryInString);
    }

    public boolean validSalary(final String salaryInString) {
        return salaryInString.matches("^[$]?[0-9]*([.,])?[0-9]?[0-9]?$");
    }

    private String printSalary(final BigDecimal salary) {
        return NumberFormat.getCurrencyInstance().format(salary);
    }

    private int receiveAge() {

        System.out.printf("%s %s: ", DefaultMessage.ENTER_WITH.getValue(), "age");

        int age;
        while (!isPositive(age = ReaderUtil.readInt())) {
            System.out.println(DefaultMessage.INVALID.getValue());
            System.out.printf("%s %s: ", DefaultMessage.ENTER_WITH.getValue(), "age");
        }

        return age;
    }

    public Employee receiveEmployee(final List<Employee> employeesFound) {

        System.out.println("More than one employee retuning! Choose one to update..");

        this.printEmployees(employeesFound);
        System.out.printf("%s %s:", DefaultMessage.ENTER_WITH.getValue(), "your option: ");

        int option = ReaderUtil.readInt();
        if (!validOption(option, employeesFound.size())) {
            throw new EmployeeException(DefaultMessage.INVALID.getValue());
        }

        return employeesFound.get(option);
    }

    private void printEmployees(final List<Employee> employeesFound) {
        employeesFound.forEach(employee ->
                System.out.printf("%d - %s\n", employeesFound.indexOf(employee), employee.getName())
        );
    }

    public void updateByOption(final EmployeeUpdateOption option, final Employee employee) {

        switch (option) {
            case NAME -> {
                final String newName = this.receiveName();
                this.updateName(employee, newName);
            }
            case DOCUMENT -> {
                final String newDocument = this.receiveDocument();
                this.updateDocument(employee, newDocument);
            }

            case SENIORITY_OF_WORK -> {
                final Departament departament = this.receiveDepartament(employee.getDepartamentsAndLevelsAndSalaries());
                this.updateLevel(employee, departament);
            }
            case SALARY_OF_WORK -> {
                final Departament departament = this.receiveDepartament(employee.getDepartamentsAndLevelsAndSalaries());
                this.updateSalary(employee, departament);
            }
        }

    }

    private void updateName(final Employee employee, String newName) {


        if (!this.validStringToUpdate(newName, employee.getName())) {
            throw new EmployeeException("Name can´t be equals to current name!");
        }

        dao.updateName(employee, newName);
        employee.setName(newName);
    }

    private void updateDocument(final Employee employee, final String newDocument) {


        if (!this.validStringToUpdate(newDocument, employee.getDocument())) {
            throw new EmployeeException("Document can´t be equals to current document!");
        }

        dao.updateDocument(employee, newDocument);
        employee.setDocument(newDocument);
    }

    private void updateLevel(final Employee employee, final Departament departament) {

        final Level oldLevel = employee.getDepartamentsAndLevelsAndSalaries()
                .get(departament).keySet().stream()
                .findFirst()
                .get();

        final List<Level> levelsWithoutOld = EnumListUtil.getEnumList(Level.class);
        levelsWithoutOld.remove(oldLevel);

        System.out.println("Receives new level..");
        final Level newLevel = this.receiveOption(levelsWithoutOld);

        dao.updateLevel(employee, departament, newLevel);

        //Updates level in entity after database table update
        final BigDecimal currentSalary = employee.getDepartamentsAndLevelsAndSalaries()
                .get(departament)
                .get(oldLevel);

        employee.getDepartamentsAndLevelsAndSalaries()
                .put(departament, Map.of(newLevel, currentSalary));
    }

    private void updateSalary(final Employee employee, final Departament departament) {

        final BigDecimal oldSalary = employee.getDepartamentsAndLevelsAndSalaries()
                .get(departament)
                .values().stream()
                .findFirst()
                .get();

        final BigDecimal newSalary = this.receiveSalary();

        if (!this.validSalaryToUpdate(oldSalary, newSalary)) {
            throw new EmployeeException("Salary can´t be equals to current salary!");
        }

        dao.updateSalary(employee, departament, newSalary);

        //Updates salary in entity after database table update - Using optional toArray
        final Level currentLevel = employee.getDepartamentsAndLevelsAndSalaries()
                .get(departament)
                .keySet()
                .toArray(Level[]::new)[0];

        employee.getDepartamentsAndLevelsAndSalaries()
                .put(departament, Map.of(currentLevel, newSalary));

    }

    public int deleteByOption(final EmployeeDeleteOption option,
                              final List<Departament> departaments) {

        return switch (option) {
            case ID -> {
                final long id = this.receiveId();
                yield this.deleteById(id);
            }
            case NAME -> {
                final String name = this.receiveName();
                yield this.deleteByName(name);
            }
            case DOCUMENT -> {
                final String document = this.receiveDocument();
                yield this.deleteByDocument(document);
            }
            case HIRE_DATE -> {
                final LocalDate hireDateWithoutTime = this.receiveDate("hire date");
                yield this.deleteByHireDate(hireDateWithoutTime);
            }
            case DEPARTAMENT -> {
                final Departament departament = this.receiveDepartament(departaments);
                yield this.deleteByDepartament(departament);
            }
        };
    }

    private int deleteById(final long id) {
        return dao.deleteById(id);
    }

    private int deleteByName(final String name) {
        return dao.deleteByName(name);
    }

    private int deleteByDocument(final String document) {
        return dao.deleteByDocument(document);
    }

    private int deleteByHireDate(final LocalDate hireDateWithoutTime) {
        return dao.deleteByHireDate(hireDateWithoutTime);
    }

    private int deleteByDepartament(final Departament departament) {
        return dao.deleteByDepartament(departament);
    }

    public boolean validOption(final int option, final int total) {
        return isPositive(option) && option < total;
    }

    private boolean isPositive(final Number num) {
        return (num instanceof Integer n)
                ? n > -1
                : num.longValue() > -1L;
    }

    private Employee mappperToSpecificEntity(final EmployeeBaseDTO dto) {
        return (dto instanceof SuperiorEmployeeDTO sed)
                ? superiorEmployeeMapper.dtoToEntity(sed)
                : (dto instanceof NormalEmployeeDTO ned)
                ? normalEmployeeMapper.dtoToEntity(ned)
                : null;
    }
}
