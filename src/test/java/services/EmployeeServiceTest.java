package services;

import dao.impl.DepartamentDAOImpl;
import dao.impl.EmployeeDAOImpl;
import domain.departament.Departament;
import domain.departament.Level;
import domain.employee.NormalEmployee;
import domain.employee.SuperiorEmployee;
import enums.employee.EmployeeType;
import enums.menu.YesOrNo;
import exceptions.DbConnectionException;
import exceptions.EmployeeException;
import mappers.DepartamentMapper;
import mappers.NormalEmployeeMapper;
import mappers.SuperiorEmployeeMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EmployeeServiceTest {

    private EmployeeService service;

    @BeforeEach
    void setUp() {
        service = new EmployeeService(
                new NormalEmployeeMapper(), new SuperiorEmployeeMapper(), new EmployeeDAOImpl()
        );
    }


    @Nested
    @DisplayName("** Validate name methods **")
    class ValidateNameTests {

        @DisplayName("Should be throw NPE Exception when name is null")
        @Test
        void givenValidateName_whenNameIsNull_thenThrowNPEException() {

            NullPointerException e = assertThrows(NullPointerException.class,
                    () -> service.validateAndFormatName(null));

            String message = "Name can´t be null!";
            assertEquals(message, e.getMessage());
        }

        @DisplayName("Should be throw Employee Exception when name contains less than three letters")
        @Test
        void givenValidateName_whenNameLengthIsLessThan3_thenThrowEmployeeException() {


            EmployeeException e = assertThrows(EmployeeException.class,
                    () -> service.validateAndFormatName("."));

            String message = "Name can´t be null!";
            assertEquals(message, e.getMessage());
        }

        @DisplayName("Should be throw Employee Exception when name contains special characters")
        @Test
        void givenValidateName_whenNameContainsSpecialCharacters_thenThrowEmployeeException() {

            EmployeeException e = assertThrows(EmployeeException.class,
                    () -> service.validateAndFormatName("Mari*"));

            String message = "Name contains special characters!";
            assertEquals(message, e.getMessage());
        }


    }

    @Nested
    @DisplayName("** Validate name methods **")
    class ValidateDocumentTests {

        @DisplayName("Should be throw NPE Exception when document is null")
        @Test
        void givenValidateDocument_whenDocumentIsNull_thenThrowNPEException() {

            NullPointerException e = assertThrows(NullPointerException.class,
                    () -> service.validateDocument(null));

            String message = "Document can´t be null!";
            assertEquals(message, e.getMessage());

        }

        @DisplayName("Should be throw EmployeeException when document does not match the CPF pattern")
        @Test
        void givenValidateDocument_whenDocumentDoesNotMeetThePattern_thenThrowEmployeeException() {

            EmployeeException e = assertThrows(EmployeeException.class,
                    () -> service.validateDocument("An invalid document"));

            String message = "Invalid document!";
            assertEquals(message, e.getMessage());

        }
    }

    @Nested
    @DisplayName("** Parse and validate date methods **")
    class ParseAndValidateDateTests {

        @DisplayName("Should be throw NPE Exception when date in string is null")
        @Test
        void givenParseAndValidateDate_whenDateInStringIsNull_thenThrowNPEException() {

            NullPointerException e = assertThrows(NullPointerException.class,
                    () -> service.parseAndValidateDate(null));

            String message = "Birth date can´t be null";
            assertEquals(message, e.getMessage());

        }


        @DisplayName("Should be return local date object when date in string format match the required pattern")
        @Test
        void givenParseAndValidateDate_whenDateInStringMeetThePattern_thenReturnLocalDateObject() {

            String dateInString = "10/10/2000";
            assertEquals(LocalDate.of(2000, 10, 10),
                    service.parseAndValidateDate(dateInString));


        }


        @DisplayName("Should be throw EmployeeException when the date does not match the CPF pattern")
        @ParameterizedTest
        @ValueSource(strings = {"0/10/2000", "32/10/2000", "-11/10/2000", "/10/2000",
                "10/0/2000", "10/33/2000", "11/-10/2000", "1/a/2000",
                "ASAS", "10*/10/3000", "", "19/10/200p", "19/10/3300"})
        void givenParseAndValidateDate_whenDateInStringNotMeetThePattern_thenThrowEmployeeException(String dateInString) {

            EmployeeException e = assertThrows(EmployeeException.class,
                    () -> service.parseAndValidateDate(dateInString));

            String message = String.format("Invalid date! %s does not meet the pattern dd/MM/yyyy!", dateInString);
            assertEquals(message, e.getMessage());

        }
    }


    @Nested
    @DisplayName("** Generate age methods **")
    class GenerateAgeTests {

        @DisplayName("Should be return the exact difference between birth date and current date when it´s employee birthday and employee has more than seventeen years old")
        @Test
        void givenGenerateAge_whenAgeIsMoreThanSeventeenYearsOldAndItsABirthDay_thenReturnExactYearsBetween() {

            int yearsCompletedToday = 18;

            LocalDate now = LocalDate.now();
            LocalDate birthDate = LocalDate.of(
                    now.getYear() - yearsCompletedToday, now.getMonthValue(), now.getDayOfMonth()
            );

            assertEquals(yearsCompletedToday, service.generateAge(birthDate));
        }

        @DisplayName("Should be return the difference between birth date and current date minus one year, because it´s not a birthday")
        @Test
        void givenGenerateAge_whenAgeIsMoreThanSeventeenYearsOldAndItsNotABirthDay_thenReturnYearsDifferenceMinusOne() {

            int yearsOldToBeComplete = 19;

            LocalDate now = LocalDate.now();
            LocalDate birthDate = LocalDate.of(
                    now.getYear() - yearsOldToBeComplete, now.getMonthValue() - 1, now.getDayOfMonth()
            );

            assertEquals(yearsOldToBeComplete - 1, service.generateAge(birthDate));
        }

        @DisplayName("Should be throw EmployeeException when age is less than eighteen years")
        @Test
        void givenGenerateAge_whenAgeIsLessThanEighteenYears_thenThrowEmployeeException() {

            int yearsOld = 17;

            LocalDate now = LocalDate.now();
            LocalDate birthDate = LocalDate.of(
                    now.getYear() - yearsOld, now.getMonthValue() - 1, now.getDayOfMonth()
            );

            EmployeeException e = assertThrows(EmployeeException.class,
                    () -> service.generateAge(birthDate));

            String message = "Employee has less than eighteen years!";
            assertEquals(message, e.getMessage());
        }

    }

    @DisplayName("** Jobs informations methods **")
    @Nested
    class JobsInformationTests {

        @DisplayName("Should be throw EmployeeException when option is negative")
        @Test
        void givenChooseElement_whenOptionIsNegative_thenThrowEmployeeException() {

            EmployeeException e = assertThrows(EmployeeException.class,
                    () -> service.chooseElement(-1, List.of()));

            String message = "Invalid choose!";
            assertEquals(e.getMessage(), message);
        }

        @DisplayName("Should be throw EmployeeException when option is more than departaments list size (test with departament)")
        @Test
        void givenChooseElement_whenOptionIsMoreThanDepartamentListSize_thenThrowEmployeeException() {

            EmployeeException e = assertThrows(EmployeeException.class,
                    () -> service.chooseElement(1, List.of(Departament.builder().build())));

            String message = "Invalid choose!";
            assertEquals(e.getMessage(), message);
        }

        @DisplayName("Should be return element (test with departament) when option is valid")
        @Test
        void givenChooseElement_whenOptionIsValid_thenReturnElementChoosed() {
            Departament departament = Departament.builder().build();
            assertEquals(departament, service.chooseElement(0, List.of(departament)));
        }

        @DisplayName("Should be throw NPEException when salary in string is null")
        @Test
        void givenValidateAndFormatSalary_whenSalaryInStringIsNull_thenThrowNPEException() {

            NullPointerException e = assertThrows(NullPointerException.class,
                    () -> service.validateAndFormatSalary(null));

            String message = "Salary can´t be null!";
            assertEquals(message, e.getMessage());
        }


        @DisplayName("Should be throw EmployeeException when salary in string is invalid")
        @ParameterizedTest
        @ValueSource(strings = {" ", "abc", "12,34a", "12,,34", "12.345",
                "12,345", "12.34.56", ".12", ",12", "12,", "12.", "12$", "12R$",
                "1,000.00", "1.000,00"})
        void givenValidateAndFormatSalary_whenSalaryInputIsInvalid_thenThrowEmployeeException(String salaryInString) {

            EmployeeException e = assertThrows(EmployeeException.class,
                    () -> service.validateAndFormatSalary(salaryInString));

            String message = "Invalid salary format!";
            assertEquals(message, e.getMessage());

        }


        @DisplayName("Should be return a valid BigDecimal value when salary in string contains comma, which will be replace by a dot")
        @Test
        void givenValidateAndFormatSalary_whenSalaryInStringIsValidAndHasComma_thenReturnBigDecimalValue() {
            assertEquals(new BigDecimal("1200.00"), service.validateAndFormatSalary("1200,00"));
        }

        @DisplayName("Should be return a valid BigDecimal value when salary in string contains dot, without replacements")
        @Test
        void givenValidateAndFormatSalary_whenSalaryInStringIsValidAndHasDot_thenReturnBigDecimalValue() {
            assertEquals(new BigDecimal("1200.00"), service.validateAndFormatSalary("1200.00"));
        }

    }

    @DisplayName("** Create employee methods **")
    @Nested
    class CreateEmployeeTests {

        @DisplayName("Should be throw NPEException when employee type is null")
        @Test
        void givenCreateEmployee_whenEmployeeTypeIsNull_thenThrowNPEException() {

            NullPointerException e = assertThrows(NullPointerException.class,
                    () -> service.createEmployee(null, null,
                            null, 0,
                            null, null));

            String message = "Employee type can´t be null!";
            assertEquals(message, e.getMessage());

        }

        @DisplayName("Should be return NormalEmployee instance when then employees type is normal")
        @Test
        void givenCreateEmployee_whenTheEmployeesTypeIsNormal_thenReturnNormalEmployeeInstance() {
            assertInstanceOf(NormalEmployee.class, service.createEmployee(null, null,
                    null, 0,
                    null, EmployeeType.NORMAL));
        }

        @DisplayName("Should be return SuperiorEmployee instance when employees type is superior")
        @Test
        void givenCreateEmployee_whenTheEmployeesTypeIsSuperior_thenReturnSuperiorEmployeeInstance() {
            assertInstanceOf(SuperiorEmployee.class, service.createEmployee(null, null,
                    null, 0,
                    null, EmployeeType.SUPERIOR));
        }

    }

    @DisplayName("** Define specific atributtes methods **")
    @Nested
    class DefineSpecificAtributtesTests {

        @DisplayName("Should be define the HasFaculty atributte of NormalEmployee as true")
        @Test
        void givenDefineHasFaculty_whenOptionIsYes_thenNormalEmployeeHasFacultyAtributteIsTrue() {

            NormalEmployee ne = NormalEmployee.builder()
                    .hasFaculty(false)
                    .build();
            assertFalse(ne.isHasFaculty());

            service.defineHasFaculty(ne, YesOrNo.YES);
            assertTrue(ne.isHasFaculty());
        }

        @DisplayName("Should be not define HasFaculty atributte of NormalEmployee as true")
        @Test
        void givenDefineHasFaculty_whenOptionIsNo_thenNormalEmployeeHasFacultyAtributteIsFalse() {

            NormalEmployee ne = NormalEmployee.builder().build();
            assertFalse(ne.isHasFaculty());

            service.defineHasFaculty(ne, YesOrNo.NO);
            assertFalse(ne.isHasFaculty());
        }


        @DisplayName("Should be throw EmployeeException when work experience is less than one year")
        @Test
        void givenDefineWorkExperience_whenWorkExperienceIsLessThanOneYear_thenThrowEmployeeException() {

            EmployeeException e = assertThrows(EmployeeException.class,
                    () -> service.defineWorkExperience(null, 1, 0));

            String message = "Should be has work experience!";
            assertEquals(message, e.getMessage());
        }

        @DisplayName("Should be throw EmployeeException when work experience is more than employee age")
        @Test
        void givenDefineWorkExperience_whenWorkExperienceIsMoreThanEmployeeAge_thenThrowEmployeeException() {

            EmployeeException e = assertThrows(EmployeeException.class,
                    () -> service.defineWorkExperience(null, 1, 2));

            String message = "Did the employee work before they were born?";
            assertEquals(message, e.getMessage());
        }

        @DisplayName("Should be throw EmployeeException when work experience is more than employee age")
        @Test
        void givenDefineWorkExperience_whenEmployeeStartedWorksUnderTheFifteenAge_thenThrowEmployeeException() {

            EmployeeException e = assertThrows(EmployeeException.class,
                    () -> service.defineWorkExperience(null, 20, 6));

            String message = "Superior Employee can´t be started work under the age of fifteen!";
            assertEquals(message, e.getMessage());
        }

    }

    @DisplayName("** Save employee methods **")
    @Nested
    class SaveEmployeeTests {

        @DisplayName("Should be set EmployeeId when save had sucess")
        @Test
        void givenSaveBaseEmployee_whenSaveHadSucess_thenEmployeeIdIsNotNull() {
            //Any employee instance
            NormalEmployee ne = NormalEmployee.builder()
                    .name("Aquino")
                    .birthDate(LocalDate.now())
                    .age(19)
                    .document("1212819181")
                    .build();

            service.saveBaseEmployee(ne);
            assertNotNull(ne.getId());
        }

        @DisplayName("Should be set EmployeeId when save had sucess")
        @Test
        void givenSaveBaseEmployee_whenEmployeeAlredyExists_thenThrowDbConnectionException() {


            //Any employee instance, after saving successfully
            SuperiorEmployee ne = SuperiorEmployee.builder()
                    .name("Aquino")
                    .birthDate(LocalDate.now())
                    .age(19)
                    .document("1212819181")
                    .build();

            DbConnectionException e = assertThrows(DbConnectionException.class,
                    () -> service.saveBaseEmployee(ne));

            String message = "Employee alredy exists!";
            assertEquals(message, e.getMessage());

        }

    }

    @DisplayName("** Find methods to show and to update**")
    @Nested
    class FindTests {
        private static DepartamentService ds;
        private Departament d;

        //Find departament to use as all Employees job
        @BeforeEach
        void setUp() {
            ds = new DepartamentService(
                    new DepartamentDAOImpl(), new DepartamentMapper()
            );
            d = ds.mapDepartaments(ds.findAll()).get(0);
        }


        @DisplayName("Should be throw EmployeeException when Employee not found by id")
        @Test
        void givenFindById_whenEmployeeNotFound_thenThrowEmployeeException() {

            EmployeeException e = assertThrows(EmployeeException.class,
                    () -> service.findById(-1));

            String message = "Employee not found!";
            assertEquals(message, e.getMessage());

        }

        @DisplayName("Should be return a NormalEmployee instance when id passed belongs to a normal employee")
        @Test
        void givenFindById_whenTheEmployeeIsFoundAndIsANormalEmployee_thenReturnNormalEmployeeInstance() {

            NormalEmployee ne = NormalEmployee.builder()
                    .name("Normal")
                    .birthDate(LocalDate.of(2010, 10, 10))
                    .age(19)
                    .document("17162932")
                    .departamentsAndLevelsAndSalaries(
                            Map.of(d, Map.of(Level.JUNIOR, new BigDecimal("1200")))
                    )
                    .hasFaculty(true)
                    .build();

            service.saveBaseEmployee(ne);
            service.saveSpecificEmployee(ne);

            assertInstanceOf(NormalEmployee.class, service.findById(ne.getId()));

        }

        @DisplayName("Should be return a SuperiorEmployee instance when id passed belongs to a superior employee")
        @Test
        void givenFindById_whenTheEmployeeIsFoundAndIsASuperiorEmployee_thenReturnSuperiorEmployeeInstance() {

            SuperiorEmployee se = SuperiorEmployee.builder()
                    .name("Superior")
                    .birthDate(LocalDate.of(2010, 10, 10))
                    .age(19)
                    .document("1918162552")
                    .departamentsAndLevelsAndSalaries(
                            Map.of(d, Map.of(Level.JUNIOR, new BigDecimal("1200")))
                    )
                    .workExperience(10)
                    .build();

            service.saveBaseEmployee(se);
            service.saveSpecificEmployee(se);

            assertInstanceOf(SuperiorEmployee.class, service.findById(se.getId()));

        }

        @DisplayName("Should be throw NPEException when name is null")
        @Test
        void givenFindByName_whenNameIsNull_thenThrowNPEException() {

            NullPointerException e = assertThrows(NullPointerException.class,
                    () -> service.findByName(null));

            String message = "Name can´t be null";
            assertEquals(message, e.getMessage());

        }

        @DisplayName("Should be throw EmployeeException when name passed not returns employee")
        @Test
        void givenFindByName_whenEmployeesNotFoundByNamePassed_thenThrowEmployeeException() {

            String name = "7&1626";
            EmployeeException e = assertThrows(EmployeeException.class,
                    () -> service.findByName(name));

            String message = String.format("Employees not found by name %s!", name);
            assertEquals(message, e.getMessage());
        }


        @DisplayName("Should be return list with size 1 when name passed only find one employee")
        @Test
        void givenFindByName_whenNamePassedReturnOneEmployee_thenListWithSizeOne() {
            //Use employee created in the find by id test
            assertEquals(1, service.findByName("Normal").size());
        }

        @DisplayName("Should be return true for list size more than one when name passed returns not singleton list")
        @Test
        void givenFindByName_whenNamePassedReturnsMoreThanOneEmployee_thenReturnListWithSizeMoreThanOne() {
            //Use employees created in the find by id test - Superior and Normal contains "r"
            assertTrue(service.findByName("r").size() > 1);
        }


        @DisplayName("Should be return list with size 1 when age passed only find one employee")
        @Test
        void givenFindByAge_whenAgePassedReturnOneEmployee_thenListWithSizeOne() {

            SuperiorEmployee se = SuperiorEmployee.builder()
                    .name("Superior11")
                    .birthDate(LocalDate.now())
                    .age(99)
                    .document("1918162552")
                    .departamentsAndLevelsAndSalaries(
                            Map.of(d, Map.of(Level.JUNIOR, new BigDecimal("1200")))
                    )
                    .workExperience(10)
                    .build();

            service.saveBaseEmployee(se);
            service.saveSpecificEmployee(se);

            assertEquals(1, service.findByAge(se.getAge()).size());
        }

        @DisplayName("Should be return true for list size more than one when age passed returns not singleton list")
        @Test
        void givenFindByAge_whenAgePassedReturnMoreThanOneEmployee_thenListWithSizeMoreThanOne() {
            //Using the age of the employees created in the find by id method
            assertTrue(service.findByAge(19).size() > 1);
        }


        @DisplayName("Should be throw NPEException when the document is null")
        @Test
        void givenFindByDocument_whenDocumentIsNull_thenThrowNPEException() {

            NullPointerException e = assertThrows(NullPointerException.class,
                    () -> service.findByDocument(null));

            String message = "Document can´t be null!";
            assertEquals(message, e.getMessage());
        }

        @DisplayName("Should be return a SuperiorEmployee when the document passed is found")
        @Test
        void givenFindByDocument_whenEmployeeIsFind_thenReturnEmployee() {

            SuperiorEmployee se = SuperiorEmployee.builder()
                    .name("Documeeent")
                    .birthDate(LocalDate.now())
                    .age(99)
                    .document("27361251625")
                    .departamentsAndLevelsAndSalaries(
                            Map.of(d, Map.of(Level.JUNIOR, new BigDecimal("1200")))
                    )
                    .workExperience(10)
                    .build();

            service.saveBaseEmployee(se);
            service.saveSpecificEmployee(se);

            assertEquals(se, service.findByDocument(se.getDocument()));

        }

        @DisplayName("Should be throw EmployeeException when the employee not found")
        @Test
        void givenFindByDocument_whenDocumentNotFound_thenThrowEmployeeException() {

            String document = "invalid";
            EmployeeException e = assertThrows(EmployeeException.class,
                    () -> service.findByDocument(document));

            String message = String.format("Employee with %s document not found!", document);
            assertEquals(message, e.getMessage());
        }

        @DisplayName("Should be throw EmployeeException when the hire date without time passed not found employees")
        @Test
        void givenFindByHireDate_whenTheListReturnedIsEmpty_thenThrowEmployeeException() {

            LocalDate hireDateWithoutTime = LocalDate.of(2000, 1, 1);
            EmployeeException e = assertThrows(EmployeeException.class,
                    () -> service.findByHireDate(hireDateWithoutTime));

            String message = String.format("Employees not found by hire date %s!", hireDateWithoutTime);
            assertEquals(message, e.getMessage());

        }

        @DisplayName("Should be return a list with one element when hire date passed found one employee")
        @Test
        void givenFindByHireDate_whenTheListReturnedHasSizeOne_thenReturnList() {

            SuperiorEmployee se = SuperiorEmployee.builder()
                    .name("Hireee")
                    .birthDate(LocalDate.of(2000, 12, 10))
                    .age(99)
                    .document("1918261652")
                    .departamentsAndLevelsAndSalaries(
                            Map.of(d, Map.of(Level.JUNIOR, new BigDecimal("1200")))
                    )
                    .workExperience(10)
                    .build();

            service.saveBaseEmployee(se);
            service.saveSpecificEmployee(se);


            assertEquals(1, service.findByHireDate(se.getHireDate().toLocalDate()).size());
        }


        @DisplayName("Should be return a list with more than one element when hire date passed found many employees")
        @Test
        void givenFindByHireDate_whenTheListReturnedHasSizeMoreThanOne_thenReturnList() {
            //Use employees created in the find by id test - Superior and Normal contains the same hire date
            assertTrue(service.findByHireDate(LocalDate.of(2010, 10, 10)).size() > 1);
        }
    }

    @DisplayName("** Update methods **")
    @Nested
    class UpdateTests {

        //Todo começar


    }


}



