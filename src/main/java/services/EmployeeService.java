package services;

import dao.interfaces.EmployeeDAO;
import domain.departament.Departament;
import domain.departament.Level;
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
import enums.menu.DefaultMessage;
import enums.menu.YesOrNo;
import exceptions.EmployeeException;
import factory.EmployeeBuilderFactory;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import mappers.NormalEmployeeMapper;
import mappers.SuperiorEmployeeMapper;
import utils.EnumListUtils;
import utils.FormatterUtils;
import utils.PrintEnumsUtils;
import utils.ReaderUtils;

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

    public String receiveInputString(final String title) {
        return ReaderUtils.readString(title);
    }

    public int receiveInputInteger(final String title) {
        return ReaderUtils.readInt(title);
    }

    public long receiveInputLong(final String title) {
        return ReaderUtils.readLong(title);
    }

    public <T extends Enum<T>> T receiveEnumElement(final String title, final Class<T> enumClass) {
        return ReaderUtils.readElement(title,
                EnumListUtils.getEnumList(enumClass));
    }

    public <T> T chooseElement(final int choose, final List<T> list) {
        if (choose < 0 || choose > list.size() - 1) throw new EmployeeException("Invalid choose!");
        return list.get(choose);
    }

    public String validateAndFormatName(final String name) {

        Objects.requireNonNull(name, "Name can´t be null!");

        if (name.length() < 3) throw new EmployeeException("Name with less than three letters!");

        if (!name.matches("^[A-Za-z]+((\\s)?((['\\-.])?([A-Za-z])+))*$")) {
            throw new EmployeeException("Name contains special characters!");
        }

        return FormatterUtils.formatName(name);

    }

    public void validateDocument(final String document) {

        Objects.requireNonNull(document, "Document can´t be null!");

        if (!document.matches("(^\\d{3}\\x2E\\d{3}\\x2E\\d{3}\\x2D\\d{2}$)")) {
            throw new EmployeeException("Invalid document!");
        }

    }

    public LocalDate parseAndValidateDate(final String dateInString) {
        Objects.requireNonNull(dateInString, "Birth date can´t be null");
        try {
            return LocalDate.parse(dateInString, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (DateTimeParseException e) {
            throw new EmployeeException(String.format("Invalid date! %s does not meet the pattern dd/MM/yyyy!", dateInString));
        }
    }

    public int generateAge(final LocalDate birthDate) {

        final Period p = Period.between(birthDate, LocalDate.now());

        final int age = (p.getMonths() == 0 && p.getDays() == 0)
                ? p.getYears()
                : p.getYears() - 1;


        if (age < 18) throw new EmployeeException("Employee has less than eighteen years!");

        return age;
    }

    public Map<Departament, Map<Level, BigDecimal>> receiveJobsInformation(final List<Departament> departaments) {

        final Map<Departament, Map<Level, BigDecimal>> dls = new HashMap<>();
        final List<Level> levels = EnumListUtils.getEnumList(Level.class);

        YesOrNo yesOrNo;
        do {
            if (departaments.isEmpty()) {
                System.out.println("There are no more departaments to allocate!");
                break;
            }

            departaments.forEach(d -> System.out.printf("%d - %s \n", departaments.indexOf(d), d.getName()));
            int choose = receiveInputInteger("departament option");
            Departament departament = chooseElement(choose, departaments);

            System.out.printf("%s %s \n", DefaultMessage.ENTER_WITH.getValue(), "your level option:");
            PrintEnumsUtils.printElements(levels);
            choose = receiveInputInteger("level option");
            Level level = chooseElement(choose, levels);

            String salaryInString = receiveInputString("employee salary (only numbers and decimal values separated by dot or comma");
            BigDecimal salary = validateAndFormatSalary(salaryInString);
            printSalary(salary);

            //Hire action
            dls.put(departament, Map.of(level, salary));

            //Removing departament of hiring
            departaments.remove(departament);

            yesOrNo = receiveEnumElement("Do you want hire this employee for other departaments? ",
                    YesOrNo.class);

        } while (yesOrNo != YesOrNo.NO);

        return dls;
    }

    //Comparing current and new value in update method
    public boolean validStringToUpdate(final String s1, final String s2) {
        return !s1.equalsIgnoreCase(s2);
    }

    public boolean validSalaryToUpdate(final BigDecimal oldSalary, final BigDecimal newSalary) {
        return oldSalary.compareTo(newSalary) != 0;
    }


    public Employee createEmployee(final String name, final String document,
                                   final LocalDate birthDate,
                                   final int age,
                                   final Map<Departament, Map<Level, BigDecimal>> dls,
                                   final EmployeeType type) {

        Objects.requireNonNull(type, "Employee type can´t be null!");

        return EmployeeBuilderFactory.newEmployeeBuilder(type)
                .name(name)
                .document(document)
                .birthDate(birthDate)
                .age(age)
                .departamentsAndLevelsAndSalaries(dls)
                .build();
    }

    public void defineSpecificAtributtes(final Employee employee) {

        if (employee instanceof NormalEmployee ne) {
            String title = String.format("Employee %s has faculty?", ne.getName());
            defineHasFaculty(ne, receiveEnumElement(title, YesOrNo.class));
        }

        //Only two types
        else if (employee instanceof SuperiorEmployee se) {
            defineWorkExperience(se, se.getAge(), receiveInputInteger("Work experience"));
        }
    }

    public void defineHasFaculty(final NormalEmployee ne, final YesOrNo option) {
        if (option == YesOrNo.YES) ne.setHasFaculty(true);
    }

    public void defineWorkExperience(final SuperiorEmployee ne, final int age, final int workExperience) {

        if (workExperience < 1) throw new EmployeeException("Should be has work experience!");

        //:)
        if (workExperience > age) throw new EmployeeException("Did the employee work before they were born?");

        if (age - workExperience < 15) {
            throw new EmployeeException("Superior Employee can´t be started work under the age of fifteen!");
        }

        ne.setWorkExperience(workExperience);
    }

    public void saveBaseEmployee(final Employee employee) {
        dao.save(employee);
    }

    public void saveSpecificEmployee(final Employee employee) {
        if (employee instanceof NormalEmployee ne) dao.saveNormalEmployee(ne);
        else if (employee instanceof SuperiorEmployee se) dao.saveSuperiorEmployee(se);
    }

    public List<Employee> findByOption(final EmployeeFindOption option) {

        return switch (option) {
            case ID -> {
                final long employeeId = receiveInputLong("employee id");
                yield Collections.singletonList(findById(employeeId));
            }
            case NAME -> {
                final String name = receiveInputString("name");
                yield findByName(name);
            }
            case AGE -> {
                final int age = receiveInputInteger("age");
                yield findByAge(age);
            }
            case DOCUMENT -> {
                final String document = receiveInputString("document");
                yield Collections.singletonList(findByDocument(document));
            }
            case HIRE_DATE -> {
                final LocalDate hireDateWithoutTime = parseAndValidateDate(
                        receiveInputString("hire date")
                );
                yield findByHireDate(hireDateWithoutTime);
            }
        };
    }

    public Employee findById(final long employeeId) {
        return dao.findById(employeeId)
                .map(this::mappperToSpecificEntity)
                .orElseThrow(() -> new EmployeeException("Employee not found!"));
    }

    public List<Employee> findByName(final String name) {

        Objects.requireNonNull(name, "Name can´t be null");

        final List<EmployeeBaseDTO> list = dao.findByName(name);
        if (list.isEmpty()) throw new EmployeeException(String.format("Employees not found by name %s!", name));

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

    public Employee findByDocument(final String document) {

        Objects.requireNonNull(document, "Document can´t be null!");

        return dao.findByDocument(document)
                .map(this::mappperToSpecificEntity)
                .orElseThrow(() -> new EmployeeException(String.format("Employee with %s document not found!", document)));
    }

    public List<Employee> findByHireDate(final LocalDate hireDateWithoutTime) {

        final List<EmployeeBaseDTO> list = dao.findByHireDate(hireDateWithoutTime);

        if (list.isEmpty()){
            throw new EmployeeException(String.format("Employees not found by hire date %s!", hireDateWithoutTime));
        }

        return list.stream()
                .map(this::mappperToSpecificEntity)
                .collect(Collectors.toList());
    }

    private void printDls(final Map<Departament, Map<Level, BigDecimal>> dls) {
        for (Map.Entry<Departament, Map<Level, BigDecimal>> map : dls.entrySet()) {
            Departament d = map.getKey();
            Level l = map.getValue().keySet().stream().findFirst().get();
            BigDecimal s = map.getValue().get(l);
            System.out.printf("Departament %sWith Level %s and salary %s \n", d, l, printSalary(s));
        }
    }


    public BigDecimal validateAndFormatSalary(final String salaryInString) {

        Objects.requireNonNull(salaryInString, "Salary can´t be null!");

        if (!salaryInString.matches("^[0-9]+([.,][0-9]{1,2})?$")) {
            throw new EmployeeException("Invalid salary format!");
        }

        return new BigDecimal(FormatterUtils.formatMoney(salaryInString));
    }

    private String printSalary(final BigDecimal salary) {
        System.out.print("Salary: ");
        return NumberFormat.getCurrencyInstance().format(salary);
    }

    public Employee receiveEmployee(final List<Employee> employeesFound) {

        System.out.println("More than one employee retuning! Choose one to update..");

        System.out.printf("%s %s:", DefaultMessage.ENTER_WITH.getValue(), "your option: ");

        int option = receiveInputInteger("employee option");
//        if (!validOption(option, employeesFound.size())) {
//            throw new EmployeeException(DefaultMessage.INVALID.getValue());
//        }

        return employeesFound.get(option);
    }

    public void updateByOption(final EmployeeUpdateOption option, final Employee employee) {

        switch (option) {
            case NAME -> {
                final String newName = receiveInputString("new name");
                updateName(employee, newName);
            }
            case DOCUMENT -> {
                final String newDocument = receiveInputString("new document");
                updateDocument(employee, newDocument);
            }

            case SENIORITY_OF_WORK -> {
                final Departament departament = null;
                updateLevel(employee, departament);
            }
            case SALARY_OF_WORK -> {
                final Departament departament = null;
                updateSalary(employee, departament);
            }
        }

    }

    private void updateName(final Employee employee, String newName) {


        if (!validStringToUpdate(newName, employee.getName())) {
            throw new EmployeeException("Name can´t be equals to current name!");
        }

        dao.updateName(employee, newName);
        employee.setName(newName);
    }

    private void updateDocument(final Employee employee, final String newDocument) {


        if (!validStringToUpdate(newDocument, employee.getDocument())) {
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

        final List<Level> levelsWithoutOld = EnumListUtils.getEnumList(Level.class);
        levelsWithoutOld.remove(oldLevel);

        System.out.println("Receives new level..");
        final Level newLevel = chooseElement(1, levelsWithoutOld);

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

        final BigDecimal newSalary = new BigDecimal(1);

        if (!validSalaryToUpdate(oldSalary, newSalary)) {
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
                final long id = receiveInputLong("id");
                yield deleteById(id);
            }
            case NAME -> {
                final String name = receiveInputString("name");
                yield deleteByName(name);
            }
            case DOCUMENT -> {
                final String document = receiveInputString("document");
                yield deleteByDocument(document);
            }
            case HIRE_DATE -> {
                final LocalDate hireDateWithoutTime = parseAndValidateDate(
                        receiveInputString("hire date")
                );
                yield deleteByHireDate(hireDateWithoutTime);
            }
            case DEPARTAMENT -> {
                final Departament departament = chooseElement(1, List.of());
                yield deleteByDepartament(departament);
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

    private Employee mappperToSpecificEntity(final EmployeeBaseDTO dto) {

        if (dto instanceof SuperiorEmployeeDTO sed) {
            return superiorEmployeeMapper.dtoToEntity(sed);
        } else if (dto instanceof NormalEmployeeDTO ned) {
            return normalEmployeeMapper.dtoToEntity(ned);
        }

        throw new EmployeeException("Invalid type");
    }
}
