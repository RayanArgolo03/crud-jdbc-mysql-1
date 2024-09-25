package services;

import model.department.Department;
import model.department.Level;
import model.employee.Employee;
import model.employee.NormalEmployee;
import model.employee.SuperiorEmployee;
import dtos.base.BaseDto;
import dtos.employee.NormalEmployeeDTO;
import dtos.EmployeeResponse;
import enums.employee.EmployeeType;
import enums.menu.YesOrNo;
import exceptions.DbConnectionException;
import exceptions.EmployeeException;
import mappers.interfaces.Mapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import repositories.interfaces.EmployeeRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private Mapper<BaseDto, Employee> mapper;
    @Mock
    private EmployeeRepository repository;
    @InjectMocks
    private EmployeeService service;

    @Nested
    @DisplayName("** Validate departmentName methods **")
    class ValidateNameTests {
        @Test
        @DisplayName("Should be throw Null Pointer Exception when the departmentName is null")
        void givenValidateAndFormatName_whenNameIsNull_thenThrowNPEException() {

            final NullPointerException e = assertThrows(NullPointerException.class,
                    () -> service.validateAndFormatName(null));

            final String expectedMessage = "Name can´t be null!";
            assertEquals(expectedMessage, e.getMessage());

        }

        @Test
        @DisplayName("Should be throw Employee Exception when the departmentName length is less than 3")
        void givenValidateAndFormatName_whenNameLengthIsLessThan3_thenThrowEmployeeException() {

            final String name = "ab";

            final EmployeeException e = assertThrows(EmployeeException.class,
                    () -> service.validateAndFormatName(name));

            final String expectedMessage = String.format("%s is a small departmentName!", name);
            assertEquals(expectedMessage, e.getMessage());

        }

        @ParameterizedTest
        @ValueSource(strings = {
                "12345", "John@Doe", "Jane_Doe", "!@#$%", "JohnDoe123", "     ", "J0hn", "Jane-Doe-", "John--Doe", "John..Doe", ".-John", "Doe-.", "Jane'.Doe", "'John", "Doe'", ".Jane", "Doe.", "John-Doe'", "-John", "Doe-", "Jane..Doe", "John.Doe'", "'Jane", "Doe'", ".John", "Doe.", "Jo--hn", "Doe--", "12345", "John@Doe", "Jane_Doe", "!@#$%", "JohnDoe123", "     ", "J0hn", "Jane-Doe-", "John--Doe", "John..Doe", ".-John", "Doe-.", "Jane'.Doe", "'John", "Doe'", ".Jane", "Doe.", "John-Doe'", "-John", "Doe-", "Jane..Doe", "John.Doe'", "'Jane", "Doe'", ".John", "Doe.", "Jo--hn", "Doe--"
        })
        @DisplayName("Should be throw Employee Exception when the departmentName contains special symbo")
        void givenValidateAndFormatName_whenNameContainsSpecialSymbol_thenThrowEmployeeException(final String name) {

            assertNotNull(name);
            final EmployeeException e = assertThrows(EmployeeException.class,
                    () -> service.validateAndFormatName(name));

            final String expectedMessage = String.format("%s contains special characters!", name);
            assertEquals(expectedMessage, e.getMessage());

        }

        @ParameterizedTest
        @ValueSource(strings = {"joAns", "jans", "AnYhASYABSnU"})
        void givenValidateAndFormatName_whenIsAValidName_thenReturnFormattedName(final String name) {
            assertNotNull(name);
            assertEquals(name.substring(0, 1).toUpperCase().concat(name.substring(1).toLowerCase()),
                    service.validateAndFormatName(name));
        }

    }

    @Nested
    @DisplayName("** Validate departmentName methods **")
    class ValidateDocumentTests {
        @Test
        @DisplayName("Should be throw Null Pointer Exception when document is null")
        void givenValidateDocument_whenDocumentIsNull_thenThrowNPEException() {

            final NullPointerException e = assertThrows(NullPointerException.class,
                    () -> service.validateDocument(null));

            final String expectedMessage = "Document can´t be null!";
            assertEquals(expectedMessage, e.getMessage());

        }

        @ParameterizedTest
        @ValueSource(strings = {"ab", "", "&", "12.345.678-90", "123.45.678-90", "123.456.78-90", "123.456.789-9", "123,456,789-90", "123.456.789/90", "123.456.789-900", "a123.456.789-90", "123.456.789-90b", "12345678990", "123456.789-90", " 123.456.789-90", "123.456.789-90 ", "123 .456.789-90", "123.456. 789-90"})
        @DisplayName("Should be throw EmployeeException when the document does not match the cpf pattern")
        void givenValidateDocument_whenDocumentNotMatchesTheCpfPattern_thenThrowEmployeeException(final String document) {

            assertNotNull(document);

            final EmployeeException e = assertThrows(EmployeeException.class,
                    () -> service.validateDocument(document));

            final String expectedMessage = String.format("Invalid document: %s does not match the pattern!", document);
            assertEquals(expectedMessage, e.getMessage());
        }

        @ParameterizedTest
        @ValueSource(strings = {"123.456.789-00", "987.654.321-99", "111.222.333-44", "555.666.777-88", "000.999.888-77"})
        @DisplayName("Should be continue when the document is valid")
        void givenValidateDocument_whenDocumentIsValid_thenDoesNotThrow(final String document) {
            assertNotNull(document);
            assertDoesNotThrow(() -> service.validateDocument(document));
        }
    }

    @Nested
    @DisplayName("** Parse and validate date methods **")
    class ParseAndValidateDateTests {

        @Test
        @DisplayName("Should be throw Null Pointer Exception when the date in string is null")
        void givenParseAndValidateDate_whenDateInStringIsNull_thenThrowNPEException() {
            final NullPointerException e = assertThrows(NullPointerException.class,
                    () -> service.parseAndValidateDate(null));

            final String expectedMessage = "Birth date can´t be null";
            assertEquals(expectedMessage, e.getMessage());
        }

        @ParameterizedTest
        @ValueSource(strings = {"a", "13-13-2099", "31-12-2020", "2020/12/31", "12/31/2020", "31/12/20", "32/01/2020", "31/13/2020", "00/12/2020", "15/00/2020", "ab/cd/efgh", "31/Dec/2020", " 31/12/2020", "31/12/2020 ", "31/ 12/2020", "31/12/2020a"})
        @DisplayName("Should be throw EmployeeException when the date in string does not match the pattern dd/MM/yyyy")
        void givenParseAndValidateDate_whenDateInStringNotMatchesThePattern_thenThrowEmployeeException(final String dateInString) {

            assertNotNull(dateInString);

            final EmployeeException e = assertThrows(EmployeeException.class,
                    () -> service.parseAndValidateDate(dateInString));

            final String expectedMessage = String.format("Invalid date! %s does not match the pattern dd/MM/yyyy!", dateInString);

            assertNotNull(e.getCause().getMessage());
            assertEquals(expectedMessage, e.getMessage());

        }

        @ParameterizedTest
        @ValueSource(strings = {"01/01/2099", "15/05/2021", "31/12/2022", "29/02/2024", "10/10/2010"})
        @DisplayName("Should be return a LocalDate object when the date in string is valid")
        void givenParseAndValidateDate_whenDateInStringIsValid_thenReturnLocalDateObject(final String dateInString) {
            assertNotNull(dateInString);
            final LocalDate expectedDate = LocalDate.parse(dateInString, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            assertEquals(expectedDate, service.parseAndValidateDate(dateInString));
        }

    }


    @Nested
    @DisplayName("** Generate age methods **")
    class GenerateAgeTests {

        private LocalDate currentDate;

        @BeforeEach
        void setUp() {
            currentDate = LocalDate.now();
        }

        @Test
        @DisplayName("Should be return the exact years difference when is aniversary and the age is more than seventeen years old")
        void givenGenerateAge_whenIsAniversaryAndAgeIsMoreThanSeventeenYears_thenReturnTheExactYearsDifference() {

            final int expectedAge = 18;
            final LocalDate birthDate = LocalDate.of(
                    currentDate.getYear() - expectedAge, currentDate.getMonthValue(), currentDate.getDayOfMonth()
            );

            assertEquals(expectedAge, service.generateAge(birthDate));
        }

        @Test
        @DisplayName("Should be return the years difference minus one when not is aniversary and the age is more than seventeen years old")
        void givenGenerateAge_whenNotIsAniversaryAndAgeIsMoreThanSeventeenYears_thenReturnTheYearsDifferenceMinusOne() {

            final int expectedAge = 19;
            final LocalDate birthDate = LocalDate.of(
                    currentDate.getYear() - expectedAge, currentDate.getMonthValue() - 1, currentDate.getDayOfMonth()
            );

            assertEquals(expectedAge - 1, service.generateAge(birthDate));
        }

        @Test
        @DisplayName("Should be throw EmployeeException when is aniversary but the age is less than eighteen years old")
        void givenGenerateAge_whenAgeIsLessThanEighteenYears_thenThrowEmployeeException() {

            final LocalDate birthDate = LocalDate.of(
                    currentDate.getYear() - 17, currentDate.getMonthValue(), currentDate.getDayOfMonth()
            );

            final EmployeeException e = assertThrows(EmployeeException.class,
                    () -> service.generateAge(birthDate));

            final String expectedMessage = "Employee has less than eighteen years!";
            assertEquals(expectedMessage, e.getMessage());
        }


    }

    @DisplayName("** Jobs informations methods **")
    @Nested
    class JobsInformationTests {

        @Test
        @DisplayName("Should be throw Null Pointer Exception when the salary in string is null")
        void givenValidateAndFormatSalary_whenSalaryInStringIsNull_thenThrowNPEException() {
            final NullPointerException e = assertThrows(NullPointerException.class,
                    () -> service.validateAndFormatSalary(null));

            final String expectedMessage = "Salary can´t be null!";
            assertEquals(expectedMessage, e.getMessage());
        }


        @ParameterizedTest
        @ValueSource(strings = {"abc", "12,345", "12.345", "12..34", "12,,34", ",123", ".123", "-123", "123.4.5", "123,4,5"})
        @DisplayName("Should be throw EmployeeException when the salary in string does not match the pattern")
        void givenValidateAndFormatSalary_whenSalaryInStringDoesNotMatchThePattern_thenThrowEmployeeException(final String salaryInString) {
            assertNotNull(salaryInString);

            final EmployeeException e = assertThrows(EmployeeException.class,
                    () -> service.validateAndFormatSalary(salaryInString));

            final String expectedMessage = String.format("%s does not match the pattern!", salaryInString);
            assertEquals(expectedMessage, e.getMessage());
        }

        @Test
        @DisplayName("Should be return salary in BigDecimal with dot when the salary in string contains a comma")
        void givenParseAndValidate_whenSalaryInStringIsValidButContainsComma_thenReturnBigDecimalFormattedSalary() {
            assertEquals(new BigDecimal("10.90"), service.validateAndFormatSalary("10,90"));
        }

        @Test
        @DisplayName("Should be return salary in BigDecimal when the salary in string contains a dot")
        void givenParseAndValidate_whenSalaryInStringIsValid_thenReturnBigDecimalSalary() {
            assertEquals(new BigDecimal("10.90"), service.validateAndFormatSalary("10.90"));
        }

    }

    @DisplayName("** Create employee methods **")
    @Nested
    class CreateEmployeeTests {
        private String name, document;
        private LocalDate birthDate;
        private int age;
        private Map<Department, Map<Level, BigDecimal>> dls;

        @BeforeEach
        void setUp() {
            name = "Any";
            document = "Any";
            birthDate = LocalDate.now();
            age = 18;
            dls = Map.of();
        }

        @Test
        @DisplayName("Should be throw Null Pointer exception when the Employee Type is null")
        void givenCreateEmployee_whenEmployeeTypeIsNull_thenThrowNPEException() {

            final NullPointerException e = assertThrows(NullPointerException.class,
                    () -> service.createEmployee(name, document, birthDate, age, dls, null));

            final String expectedMessage = "Employee type can´t be null!";
            assertEquals(expectedMessage, e.getMessage());
        }

        @Test
        @DisplayName("Should be return a NormalEmployee when the Employee Type is Normal")
        void givenCreateEmployee_whenEmployeeTypeIsNormalEmployee_thenReturnANormalEmployee() {

            final EmployeeType type = EmployeeType.NORMAL;

            final Employee employee = service.createEmployee(name, document, birthDate, age, dls, type);

            assertInstanceOf(NormalEmployee.class, employee);
        }

        @Test
        @DisplayName("Should be return a SuperiorEmployee when the Employee Type is Superior")
        void givenCreateEmployee_whenEmployeeTypeIsSuperior_thenReturnASuperiorEmployee() {

            final EmployeeType type = EmployeeType.SUPERIOR;

            final Employee employee = service.createEmployee(name, document, birthDate, age, dls, type);

            assertInstanceOf(SuperiorEmployee.class, employee);
        }


    }

    @DisplayName("** Define has faculty methods **")
    @Nested
    class DefineHasFacultyTests {
        private NormalEmployee ne;

        @BeforeEach
        void setUp() {
            ne = NormalEmployee.builder().build();
        }

        @Test
        @DisplayName("Should be set true in has faculty atributte when the NormalEmploye has faculty")
        void givenDefineHasFaculty_whenNormalEmployeeHasFaculty_thenSetTrueInAtributte() {
            assertFalse(ne.isHasFaculty());
            service.defineHasFaculty(ne, YesOrNo.YES);
            assertTrue(ne.isHasFaculty());
        }

        @Test
        @DisplayName("Do nothing when NormalEmployee not has faculty")
        void givenDefineHasFaculty_whenNormalEmployeeNotHasFaculty_thenDoNothing() {
            assertFalse(ne.isHasFaculty());
            service.defineHasFaculty(ne, YesOrNo.NO);
            assertFalse(ne.isHasFaculty());
        }

    }

    @DisplayName("** Define work experience methods **")
    @Nested
    class DefineWorkExperienceTests {

        private SuperiorEmployee se;
        private int age;

        @BeforeEach
        void setUp() {
            se = SuperiorEmployee.builder().build();
            age = 19;
        }

        @Test
        @DisplayName("Should be throw Employee Exception when the work experience is less than one year")
        void givenDefineWorkExperience_whenWorkExperienceIsLessThanOneYear_thenThrowEmployeeException() {

            final int workExperience = -1;

            final EmployeeException e = assertThrows(EmployeeException.class,
                    () -> service.defineWorkExperience(se, age, workExperience));

            final String expectedMessage = "Should be has work experience!";
            assertEquals(expectedMessage, e.getMessage());

        }

        @Test
        @DisplayName("Should be throw Employee Exception when the work experience is more than the employee age")
        void givenDefineWorkExperience_whenWorkExperienceIsMoreThanTheEmployeeAge_thenThrowEmployeeException() {

            final int workExperience = 20;

            final EmployeeException e = assertThrows(EmployeeException.class,
                    () -> service.defineWorkExperience(se, age, workExperience));

            final String expectedMessage = "Did the employee work before they were born?";
            assertEquals(expectedMessage, e.getMessage());

        }

        @Test
        @DisplayName("Should be throw Employee Exception when the employee age minus the work experience is less than fifteen years old")
        void givenDefineWorkExperience_whenEmployeeStartedWorkUnderFifteenYearsOld_thenThrowEmployeeException() {

            final int workExperience = 5;

            final EmployeeException e = assertThrows(EmployeeException.class,
                    () -> service.defineWorkExperience(se, age, workExperience));

            final String expectedMessage = "Superior Employee can´t be started work under the age of fifteen! (15 years old)";
            assertEquals(expectedMessage, e.getMessage());

        }

        @Test
        @DisplayName("Should be set Work Experience in employee when is valid")
        void givenDefineWorkExperience_whenWorkExperienceIsValid_thenSetWorkExperienceInEmployee() {

            final int workExperience = 4;

            assertEquals(0, se.getWorkExperience());

            service.defineWorkExperience(se, age, workExperience);
            assertEquals(workExperience, se.getWorkExperience());
        }

    }

    @DisplayName("** Save base employee methods **")
    @Nested
    class SaveBaseEmployeeTests {

        private NormalEmployee ne;

        @BeforeEach
        void setUp() {
            ne = NormalEmployee.builder().build();
        }

        @Test
        @DisplayName("Should be throw Employee Exception when the Employee already exists")
        void givenSaveBaseEmployee_whenEmployeeAlreadyExists_thenThrowEmployeeException() {

            final String expectedCauseMessage = "Employee already exists!";
            doThrow(new DbConnectionException(expectedCauseMessage)).when(repository).save(ne);

            final EmployeeException e = assertThrows(EmployeeException.class,
                    () -> service.saveBaseEmployee(ne));

            assertNotNull(e.getCause());
            assertEquals(expectedCauseMessage, e.getCause().getMessage());

            final String expectedMessage = String.format("Error in save: %s", expectedCauseMessage);
            assertEquals(expectedMessage, e.getMessage());

            verify(repository).save(ne);

        }

        @Test
        @DisplayName("Should be set the employee id when the employee is saved")
        void givenSaveBaseEmployee_whenEmployeeIsSaved_thenSetEmployeeId() {

            final Long expectedId = 1L;

            doAnswer(param -> {
                        NormalEmployee ne = param.getArgument(0);
                        ne.setId(expectedId);
                        return null;
                    }
            ).when(repository).save(ne);

            service.saveBaseEmployee(ne);
            assertEquals(expectedId, ne.getId());

            verify(repository).save(ne);
        }

    }

    @DisplayName("** Save specific employee methods")
    @Nested
    class SaveSpecificEmployeeTests {
        @Test
        @DisplayName("Should be call SaveNormalEmployee method when the employee is a NormalEmployee")
        void givenSaveSpecificEmployee_whenEmployeeIsANormalEmployee_thenCallSaveNormalEmployee() {

            final NormalEmployee ne = NormalEmployee.builder().build();
            doNothing().when(repository).saveNormalEmployee(ne);

            service.saveSpecificEmployee(ne);

            verify(repository, times(1)).saveNormalEmployee(ne);

        }

        @Test
        @DisplayName("Should be call SaveSuperiorEmployee method when the employee is a SuperiorEmployee")
        void givenSaveSpecificEmployee_whenEmployeeIsASuperiorEmployee_thenCallSaveSuperiorEmployee() {

            final SuperiorEmployee se = SuperiorEmployee.builder().build();
            doNothing().when(repository).saveSuperiorEmployee(se);

            service.saveSpecificEmployee(se);

            verify(repository, times(1)).saveSuperiorEmployee(se);

        }

    }


    @DisplayName("** Find by id **")
    @Nested
    class FindByIdTests {

        private long employeeId;

        @BeforeEach
        void setUp() {
            employeeId = 1L;
        }

        @Test
        @DisplayName("Should be throw Employee Exception when the employee is not found")
        void givenFindById_whenEmployeeNotFound_thenThrowEmployeeException() {

            when(repository.findById(employeeId)).thenReturn(Optional.empty());

            final EmployeeException e = assertThrows(EmployeeException.class,
                    () -> service.findById(employeeId));

            final String expectedMessage = String.format("Employee with id %d not found!", employeeId);
            assertEquals(expectedMessage, e.getMessage());

            verify(repository).findById(employeeId);

        }

        @Test
        @DisplayName("Should be return a singleton list with a NormalEmployee mapped when the Employee found is a NormalEmployee")
        void givenFindById_whenEmployeeIsFoundAndIsNormalEmployee_thenReturnASingletonListWithTheNormalEmployeeMapped() {

            final NormalEmployeeDTO dto = NormalEmployeeDTO.builder().id(employeeId).build();
            final NormalEmployee expectedEmployee = NormalEmployee.builder().id(dto.getId()).build();

            when(repository.findById(employeeId)).thenReturn(Optional.of(dto));
            when(mapper.dtoToEntity(dto)).thenReturn(expectedEmployee);

            assertEquals(Collections.singletonList(expectedEmployee), service.findById(employeeId));

            verify(repository).findById(employeeId);
            verify(mapper).dtoToEntity(dto);
        }

        @Test
        @DisplayName("Should be return a SuperiorEmployee mapped when the Employee found by id is a SuperiorEmployee")
        void givenFindById_whenEmployeeIsFoundAndIsSuperiorEmployee_thenReturnASingletonListWithTheSuperiorEmployeeMapped() {

            final EmployeeResponse dto = EmployeeResponse.builder().id(employeeId).build();

            when(repository.findById(employeeId)).thenReturn(Optional.of(dto));

            doAnswer(input -> {
                        EmployeeResponse param = input.getArgument(0);
                        return SuperiorEmployee.builder()
                                .id(param.getId())
                                .build();
                    }
            ).when(mapper).dtoToEntity(dto);

            final List<Employee> list = service.findById(employeeId);

            assertEquals(1, list.size());
            assertInstanceOf(SuperiorEmployee.class, list.get(0));
            assertEquals(employeeId, list.get(0).getId());

            verify(repository).findById(employeeId);
            verify(mapper).dtoToEntity(dto);
        }

    }

    @DisplayName("** Find by departmentName **")
    @Nested
    class FindByNameTests {

        private String name;

        @BeforeEach
        void setUp() {
            name = "Michelangelo";
        }

        @Test
        @DisplayName("Should be throw NullPointerException when the departmentName is null")
        void givenFindByName_whenNameIsNull_thenThrowNPEException() {

            final NullPointerException e = assertThrows(NullPointerException.class,
                    () -> service.findByName(null));

            final String expectedMessage = "Name can´t be null";
            assertEquals(expectedMessage, e.getMessage());
        }

        @Test
        @DisplayName("Should be throw EmployeeException when employees not found by departmentName passed")
        void givenFindByName_whenEmployeesNotFoundByName_thenThrowEmployeeException() {

            when(repository.findByName(name)).thenReturn(List.of());

            final EmployeeException e = assertThrows(EmployeeException.class,
                    () -> service.findByName(name));

            final String expectedMessage = String.format("Employees not found by departmentName %s!", name);
            assertEquals(expectedMessage, e.getMessage());
        }

        @Test
        @DisplayName("Should be return a list with mapped employees when the departmentName passed found different employees")
        void givenFindByName_whenFoundTwoEmployeesWithDifferentType_thenReturnMappedEmployeeList() {

            final int expectedSize = 2;

            final EmployeeResponse sDto = EmployeeResponse.builder().name(name).build();
            final NormalEmployeeDTO nDto = NormalEmployeeDTO.builder().name(name).build();

            when(repository.findByName(name)).thenReturn(List.of(sDto, nDto));

            when(mapper.dtoToEntity(sDto)).thenReturn(
                    SuperiorEmployee.builder().name(sDto.getName()).build()
            );

            when(mapper.dtoToEntity(nDto)).thenReturn(
                    NormalEmployee.builder().name(nDto.getName()).build()
            );

            final List<Employee> list = service.findByName(name);

            assertEquals(expectedSize, list.size());
            assertInstanceOf(SuperiorEmployee.class, list.get(0));
            assertEquals(name, list.get(0).getName());
            assertInstanceOf(NormalEmployee.class, list.get(1));
            assertEquals(name, list.get(1).getName());

            verify(repository).findByName(name);
            verify(mapper, times(2)).dtoToEntity(any());

        }

    }

    @DisplayName("** Find by document **")
    @Nested
    class FindByDocumentTests {

        private String document;

        @BeforeEach
        void setUp() {
            document = "121221212";
        }

        @Test
        @DisplayName("Should be throw NullPointerException when the document is null")
        void givenFindByDocument_whenDocumentIsNull_thenThrowNPEException() {

            final NullPointerException e = assertThrows(NullPointerException.class,
                    () -> service.findByDocument(null));

            final String expectedMessage = "Document can´t be null!";
            assertEquals(expectedMessage, e.getMessage());
        }

        @Test
        @DisplayName("Should be throw EmployeeException when employees not found by document passed")
        void givenFindByDocument_whenEmployeesNotFoundByDocument_thenThrowEmployeeException() {

            when(repository.findByDocument(document)).thenReturn(Optional.empty());

            final EmployeeException e = assertThrows(EmployeeException.class,
                    () -> service.findByDocument(document));

            final String expectedMessage = String.format("Employee not found by document %s!", document);
            assertEquals(expectedMessage, e.getMessage());
        }

        @Test
        @DisplayName("Should be return a singleton list with the NormalEmployee mapped when the Employee found is a NormalEmployee")
        void givenFindByDocument_whenEmployeeFoundByIsANormalEmployee_thenReturnASingletonListWithTheNormalEmployeeMapped() {

            final NormalEmployeeDTO dto = NormalEmployeeDTO.builder().document(document).build();
            final NormalEmployee employeeExpected = NormalEmployee.builder().document(dto.getDocument()).build();

            when(repository.findByDocument(document)).thenReturn(Optional.of(dto));
            when(mapper.dtoToEntity(dto)).thenReturn(employeeExpected);

            assertEquals(Collections.singletonList(employeeExpected), service.findByDocument(document));

            verify(repository).findByDocument(document);
            verify(mapper).dtoToEntity(dto);
        }

        @Test
        @DisplayName("Should be return a singleton list with the SuperiorEmployee mapped when the Employee found is a SuperiorEmployee")
        void givenFindByDocument_whenEmployeeFoundByIsASuperiorEmployee_thenReturnASingletonListWithTheSuperiorEmployeeMapped() {

            final EmployeeResponse dto = EmployeeResponse.builder().document(document).build();

            when(repository.findByDocument(document)).thenReturn(Optional.of(dto));

            doAnswer(input -> {
                        EmployeeResponse param = input.getArgument(0);
                        return SuperiorEmployee.builder()
                                .document(param.getDocument())
                                .build();
                    }
            ).when(mapper).dtoToEntity(dto);

            final List<Employee> list = service.findByDocument(document);

            assertEquals(1, list.size());
            assertInstanceOf(SuperiorEmployee.class, list.get(0));
            assertEquals(document, list.get(0).getDocument());

            verify(repository).findByDocument(document);
            verify(mapper).dtoToEntity(dto);
        }

    }

    @DisplayName("** Find by age **")
    @Nested
    class FindByAgeTests {
        private int age;

        @BeforeEach
        void setUp() {
            age = 10;
        }

        @Test
        @DisplayName("Should be throw EmployeeException when employees not found by age passed")
        void givenFindByAge_whenEmployeesNotFoundByAge_thenThrowEmployeeException() {

            when(repository.findByAge(age)).thenReturn(Collections.emptyList());

            final EmployeeException e = assertThrows(EmployeeException.class,
                    () -> service.findByAge(age));

            final String expectedMessage = String.format("Employees not found by age %d!", age);
            assertEquals(expectedMessage, e.getMessage());
        }

        @Test
        @DisplayName("Should be return a singleton list with the NormalEmployee mapped when the Employee found is a NormalEmployee")
        void givenFindByAge_whenEmployeeFoundByIsANormalEmployee_thenReturnASingletonListWithTheNormalEmployeeMapped() {

            final NormalEmployeeDTO dto = NormalEmployeeDTO.builder().age(age).build();
            final NormalEmployee employeeExpected = NormalEmployee.builder().age(dto.getAge()).build();

            when(repository.findByAge(age)).thenReturn(List.of(dto));
            when(mapper.dtoToEntity(dto)).thenReturn(employeeExpected);

            assertEquals(Collections.singletonList(employeeExpected), service.findByAge(age));

            verify(repository).findByAge(age);
            verify(mapper).dtoToEntity(dto);
        }

        @Test
        @DisplayName("Should be return a singleton list with the SuperiorEmployee mapped when the Employee found is a SuperiorEmployee")
        void givenFindByAge_whenEmployeeFoundByIsASuperiorEmployee_thenReturnASingletonListWithTheSuperiorEmployeeMapped() {

            final EmployeeResponse dto = EmployeeResponse.builder().age(age).build();

            when(repository.findByAge(age)).thenReturn(List.of(dto));

            doAnswer(input -> {
                        EmployeeResponse param = input.getArgument(0);
                        return SuperiorEmployee.builder()
                                .age(age)
                                .build();
                    }
            ).when(mapper).dtoToEntity(dto);

            final List<Employee> list = service.findByAge(age);

            assertEquals(1, list.size());
            assertInstanceOf(SuperiorEmployee.class, list.get(0));
            assertEquals(age, list.get(0).getAge());

            verify(repository).findByAge(age);
            verify(mapper).dtoToEntity(dto);
        }

    }

    @DisplayName("** Find by hire date **")
    @Nested
    class FindByHireDateTests {
        private LocalDate hireDateWithoutTime;

        @BeforeEach
        void setUp() {
            hireDateWithoutTime = LocalDate.now();
        }

        @Test
        @DisplayName("Should be throw EmployeeException when employees not found by hire date passed")
        void givenFindByHireDate_whenEmployeesNotFoundByHireDate_thenThrowEmployeeException() {

            when(repository.findByHireDate(hireDateWithoutTime)).thenReturn(Collections.emptyList());

            final EmployeeException e = assertThrows(EmployeeException.class,
                    () -> service.findByHireDate(hireDateWithoutTime));

            final String expectedMessage = String.format("Employees not found by hire date %s!", DateTimeFormatter.ofPattern("dd/MM/yyyy").format(hireDateWithoutTime));
            System.out.println(expectedMessage);
            assertEquals(expectedMessage, e.getMessage());
        }

        @Test
        @DisplayName("Should be return a singleton list with the NormalEmployee mapped when the Employee found is a NormalEmployee")
        void givenFindByHireDate_whenEmployeeFoundByIsANormalEmployee_thenReturnASingletonListWithTheNormalEmployeeMapped() {

            final LocalDateTime hireDate = LocalDateTime.of(hireDateWithoutTime, LocalTime.now());
            final NormalEmployeeDTO dto = NormalEmployeeDTO.builder().hireDate(hireDate).build();
            final NormalEmployee employeeExpected = NormalEmployee.builder().createdDate(hireDate).build();

            when(repository.findByHireDate(hireDateWithoutTime)).thenReturn(List.of(dto));
            when(mapper.dtoToEntity(dto)).thenReturn(employeeExpected);

            assertEquals(Collections.singletonList(employeeExpected), service.findByHireDate(hireDateWithoutTime));

            verify(repository).findByHireDate(hireDateWithoutTime);
            verify(mapper).dtoToEntity(dto);
        }

        @Test
        @DisplayName("Should be return a singleton list with the SuperiorEmployee mapped when the Employee found is a SuperiorEmployee")
        void givenFindByHireDate_whenEmployeeFoundByIsASuperiorEmployee_thenReturnASingletonListWithTheSuperiorEmployeeMapped() {

            final LocalDateTime hireDate = LocalDateTime.of(hireDateWithoutTime, LocalTime.now());
            final EmployeeResponse dto = EmployeeResponse.builder().hireDate(hireDate).build();

            when(repository.findByHireDate(hireDateWithoutTime)).thenReturn(List.of(dto));

            doAnswer(input -> {
                        EmployeeResponse param = input.getArgument(0);
                        return SuperiorEmployee.builder()
                                .createdDate(hireDate)
                                .build();
                    }
            ).when(mapper).dtoToEntity(dto);

            final List<Employee> list = service.findByHireDate(hireDateWithoutTime);

            assertEquals(1, list.size());
            assertInstanceOf(SuperiorEmployee.class, list.get(0));
            assertEquals(hireDate, list.get(0).getCreatedDate());

            verify(repository).findByHireDate(hireDateWithoutTime);
            verify(mapper).dtoToEntity(dto);
        }

    }

    @Nested
    @DisplayName("** Update departmentName **")
    class UpdateNameAndDocumentTests {

        private Employee employee;

        @BeforeEach
        void setUp() {
            employee = NormalEmployee.builder().name("Boothman")
                    .document("191.918.291-02")
                    .build();
        }

        @Test
        @DisplayName("Should be throw NullPointerException when the new departmentName is null")
        void givenUpdateName_whenNewNameIsNull_thenThrowNPEException() {

            final NullPointerException e = assertThrows(NullPointerException.class,
                    () -> service.updateName(employee, null));

            final String expectedMessage = "New departmentName can´t be null!";
            assertEquals(expectedMessage, e.getMessage());

        }

        @Test
        @DisplayName("Should be throw EmployeeException when the new departmentName is equals to the current departmentName")
        void givenUpdateName_whenNewNameIsEqualsToCurrentName_thenThrowEmployeeException() {

            final EmployeeException e = assertThrows(EmployeeException.class,
                    () -> service.updateName(employee, employee.getName()));

            final String expectedMessage = String.format("Name %s can´t be equals to current departmentName!", employee.getName());
            assertEquals(expectedMessage, e.getMessage());

        }

        @Test
        @DisplayName("Should be set new departmentName in Employee when the new departmentName is valid")
        void givenUpdateName_whenNewNameIsValid_thenSetNewNameInEmployee() {

            final String expectedNewName = "LuisXIV";

            assertNotEquals(expectedNewName, employee.getName());

            doAnswer(input -> {
                        Employee employeeUpdated = input.getArgument(0);
                        employeeUpdated.setName(expectedNewName);
                        return null;
                    }
            ).when(repository).updateName(employee, expectedNewName);

            service.updateName(employee, expectedNewName);
            assertEquals(expectedNewName, employee.getName());

            verify(repository).updateName(employee, expectedNewName);

        }

        @Test
        @DisplayName("Should be throw NullPointerException when the new document is null")
        void givenUpdateDocument_whenNewDocumentIsNull_thenThrowNPEException() {

            final NullPointerException e = assertThrows(NullPointerException.class,
                    () -> service.updateDocument(employee, null));

            final String expectedMessage = "New document can´t be null!";
            assertEquals(expectedMessage, e.getMessage());

        }

        @Test
        @DisplayName("Should be throw EmployeeException when the new document is equals to the current document")
        void givenUpdateDocument_whenNewDocumentIsEqualsToCurrentDocument_thenThrowEmployeeException() {

            final EmployeeException e = assertThrows(EmployeeException.class,
                    () -> service.updateDocument(employee, employee.getDocument()));

            final String expectedMessage = String.format("Document %s can´t be equals to current document!", employee.getDocument());
            assertEquals(expectedMessage, e.getMessage());

        }

        @Test
        @DisplayName("Should be set new document in Employee when the new document is valid")
        void givenUpdateDocument_whenNewDocumentIsValid_thenSetNewDocumentInEmployee() {

            //Document is not important given that the real method is not called
            final String expectedNewDocument = "182712717";

            assertNotEquals(expectedNewDocument, employee.getDocument());

            doAnswer(input -> {
                        Employee employeeUpdated = input.getArgument(0);
                        employeeUpdated.setDocument(expectedNewDocument);
                        return null;
                    }
            ).when(repository).updateDocument(employee, expectedNewDocument);

            service.updateDocument(employee, expectedNewDocument);

            assertEquals(expectedNewDocument, employee.getDocument());

            verify(repository).updateDocument(employee, expectedNewDocument);

        }


    }


    @Nested
    @DisplayName("** Update level and salary **")
    class UpdateLevelAndSalaryTests {
        private NormalEmployee employee;
        private Department department;
        private Level oldLevel;
        private BigDecimal oldSalary;

        @BeforeEach
        void setUp() {

            department = Department.builder().name("Foo").build();
            oldLevel = Level.JUNIOR;
            oldSalary = new BigDecimal("1200");

            //Mutable map to put the new level updated
            Map<Department, Map<Level, BigDecimal>> dls = new HashMap<>();
            dls.put(department, Map.of(oldLevel, oldSalary));

            employee = NormalEmployee.builder()
                    .departmentsAndLevelsAndSalaries(dls)
                    .build();
        }

        @Test
        @DisplayName("Should be throw NullPointerException when the new level is null")
        void givenUpdateSeniorityofWork_whenNewLevelIsNull_thenThrowNPEException() {

            final NullPointerException e = assertThrows(NullPointerException.class,
                    () -> service.updateLevel(employee, department, null, oldLevel));

            final String expectedMessage = "New level can´t be null!";
            assertEquals(expectedMessage, e.getMessage());

        }

        @Test
        @DisplayName("Should be update the level in map when the updated is completed")
        void givenUpdateSeniorityofWork_whenTheEmployeeLevelIsUpdated_thenPutNewLevelInTheEmployeeMap() {

            final Level expectedNewLevel = Level.MID;
            assertNotEquals(expectedNewLevel, oldLevel);

            doAnswer(params -> {
                        params.getArgument(0, NormalEmployee.class)
                                .getDepartmentsAndLevelsAndSalaries()
                                .put(department, Map.of(expectedNewLevel, oldSalary));

                        return null;
                    }
            ).when(repository).updateLevel(employee, department, expectedNewLevel, oldLevel);

            service.updateLevel(employee, department, expectedNewLevel, oldLevel);

            final Level employeeNewLevel = new ArrayList<>(employee.getDepartmentsAndLevelsAndSalaries()
                    .get(department)
                    .keySet()).get(0);

            assertEquals(expectedNewLevel, employeeNewLevel);

            verify(repository).updateLevel(employee, department, expectedNewLevel, oldLevel);

        }

        @Test
        @DisplayName("Should be throw EmployeeException when the new salary is equals to current salary")
        void givenUpdateSalary_whenTheNewSalaryIsEqualsToCurrentSalary_theThrowEmployeeException() {

            final BigDecimal newSalary = new BigDecimal(oldSalary.toString());

            final EmployeeException e = assertThrows(EmployeeException.class,
                    () -> service.updateSalary(employee, department, newSalary, oldSalary));

            final String expectedMessage = "Salary can´t be equals to current salary!";
            assertEquals(expectedMessage, e.getMessage());
        }


        @Test
        @DisplayName("Should be update the salary in map when the updated is completed")
        void givenUpdateSalary_whenTheSalaryIsUpdated_thenPutNewSalaryInTheEmployeeMap() {

            final BigDecimal expectedNewSalary = new BigDecimal(oldSalary.toString() + "1");
            assertNotEquals(expectedNewSalary, oldSalary);

            doAnswer(params -> {
                        params.getArgument(0, NormalEmployee.class)
                                .getDepartmentsAndLevelsAndSalaries()
                                .put(department, Map.of(oldLevel, expectedNewSalary));

                        return null;
                    }
            ).when(repository).updateSalary(employee, department, expectedNewSalary, oldSalary);

            service.updateSalary(employee, department, expectedNewSalary, oldSalary);

            final BigDecimal employeeNewSalary = employee
                    .getDepartmentsAndLevelsAndSalaries()
                    .get(department)
                    .get(oldLevel);

            assertEquals(expectedNewSalary, employeeNewSalary);

            verify(repository).updateSalary(employee, department, expectedNewSalary, oldSalary);
        }

    }


    @Nested
    @DisplayName("** Delete by id methods **")
    class DeleteByIdTests {

        private long id;

        @BeforeEach
        void setUp() {
            id = 1;
        }

        @Test
        @DisplayName("Should be throw EmployeeException when employee not found by id")
        void givenDeleteById_whenEmployeeNotFoundById_thenThrowEmployeeException() {

            final String expectedCauseMessage = String.format("No employees found by id %d, nobody sacked!", id);
            doThrow(new DbConnectionException(expectedCauseMessage)).when(repository).deleteById(id);

            final EmployeeException e = assertThrows(EmployeeException.class,
                    () -> service.deleteById(id));

            assertNotNull(e.getCause());
            assertEquals(expectedCauseMessage, e.getCause().getMessage());

            final String expectedMessage = String.format("Error: %s", expectedCauseMessage);
            assertEquals(expectedMessage, e.getMessage());

            verify(repository).deleteById(id);
        }

        @Test
        @DisplayName("Should be return 1 when employee has been deleted by id")
        void givenDeleteById_whenEmployeeHasBeenDeleted_thenReturnOne() {
            when(repository.deleteById(1)).thenReturn(1);
            assertEquals(1, service.deleteById(id));
            verify(repository).deleteById(id);
        }
    }

    @Nested
    @DisplayName("** Delete by departmentName methods **")
    class DeleteByNameTests {

        private String name;

        @BeforeEach
        void setUp() {
            name = "Marx";
        }

        @Test
        @DisplayName("Should be throw EmployeeException when no have employees found by departmentName")
        void givenDeleteByName_whenNoHasEmployeesFoundByName_thenThrowEmployeeException() {

            final String expectedCauseMessage = String.format("No employees found by departmentName %s, nobody sacked!", name);
            doThrow(new DbConnectionException(expectedCauseMessage)).when(repository).deleteByName(name);

            final EmployeeException e = assertThrows(EmployeeException.class,
                    () -> service.deleteByName(name));

            assertNotNull(e.getCause());
            assertEquals(expectedCauseMessage, e.getCause().getMessage());

            final String expectedMessage = String.format("Error: %s", expectedCauseMessage);
            assertEquals(expectedMessage, e.getMessage());

            verify(repository).deleteByName(name);
        }

        @Test
        @DisplayName("Should be return 1 when employee has been deleted by departmentName")
        void givenDeleteByName_whenOneEmployeeHasBeenDeletedByName_thenReturnOne() {
            when(repository.deleteByName(name)).thenReturn(1);
            assertEquals(1, service.deleteByName(name));
            verify(repository).deleteByName(name);
        }

        @Test
        @DisplayName("Should be return more than 1 when employees has been deleted by departmentName")
        void givenDeleteByName_whenEmployeeHasBeenDeleted_thenReturnMoreThanOne() {

            when(repository.deleteByName(name)).thenReturn(
                    new Random().nextInt(2, 5)
            );

            assertTrue(service.deleteByName(name) > 1);

            verify(repository).deleteByName(name);
        }
    }

    @Nested
    @DisplayName("** Delete by document methods **")
    class DeleteByDocumentTests {

        private String document;

        @BeforeEach
        void setUp() {
            document = "12112121";
        }

        @Test
        @DisplayName("Should be throw EmployeeException when no have employees found by document")
        void givenDeleteByDocument_whenNoEmployeesFoundByDocument_thenThrowEmployeeException() {

            final String expectedCauseMessage = String.format("No employees found by document %s, nobody sacked!", document);
            doThrow(new DbConnectionException(expectedCauseMessage)).when(repository).deleteByDocument(document);

            final EmployeeException e = assertThrows(EmployeeException.class,
                    () -> service.deleteByDocument(document));

            assertNotNull(e.getCause());
            assertEquals(expectedCauseMessage, e.getCause().getMessage());

            final String expectedMessage = String.format("Error: %s", expectedCauseMessage);
            assertEquals(expectedMessage, e.getMessage());

            verify(repository).deleteByDocument(document);
        }

        @Test
        @DisplayName("Should be return 1 when employee has been deleted by document, unique")
        void givenDeleteByDocument_whenEmployeeHasBeenDeleted_thenReturnOne() {
            when(repository.deleteByDocument(document)).thenReturn(1);
            assertEquals(1, service.deleteByDocument(document));
            verify(repository).deleteByDocument(document);
        }
    }

    @Nested
    @DisplayName("** Delete by hire date methods **")
    class DeleteByHireDateTests {

        private LocalDate hireDateWithoutTime;

        @BeforeEach
        void setUp() {
            hireDateWithoutTime = LocalDate.now();
        }

        @Test
        @DisplayName("Should be throw EmployeeException when no have employees found by hire date")
        void givenDeleteByHireDate_whenNoEmployeesFoundByName_thenThrowEmployeeException() {

            final String expectedCauseMessage = String.format("No employees found by hire date %s, nobody sacked!",
                    DateTimeFormatter.ofPattern("dd/MM/yyyy").format(hireDateWithoutTime));

            doThrow(new DbConnectionException(expectedCauseMessage)).when(repository).deleteByHireDate(hireDateWithoutTime);

            final EmployeeException e = assertThrows(EmployeeException.class,
                    () -> service.deleteByHireDate(hireDateWithoutTime));

            assertNotNull(e.getCause());
            assertEquals(expectedCauseMessage, e.getCause().getMessage());

            final String expectedMessage = String.format("Error: %s", expectedCauseMessage);
            assertEquals(expectedMessage, e.getMessage());

            verify(repository).deleteByHireDate(hireDateWithoutTime);
        }

        @Test
        @DisplayName("Should be return 1 when employee has been deleted by hire date")
        void givenDeleteByHireDate_whenEmployeeHasBeenDeleted_thenReturnOne() {
            when(repository.deleteByHireDate(hireDateWithoutTime)).thenReturn(1);
            assertEquals(1, service.deleteByHireDate(hireDateWithoutTime));
            verify(repository).deleteByHireDate(hireDateWithoutTime);
        }

        @Test
        @DisplayName("Should be return more than 1 when employees has been deleted by hire")
        void givenDeleteByName_whenEmployeeHasBeenDeleted_thenReturnMoreThanOne() {

            when(repository.deleteByHireDate(hireDateWithoutTime)).thenReturn(
                    new Random().nextInt(2, 5)
            );

            assertTrue(service.deleteByHireDate(hireDateWithoutTime) > 1);

            verify(repository).deleteByHireDate(hireDateWithoutTime);
        }
    }

    @Nested
    @DisplayName("** Delete by department methods **")
    class DeleteByDepartmentTests {

        private Department department;

        @BeforeEach
        void setUp() {
            department = Department.builder().build();
        }

        @Test
        @DisplayName("Should be throw EmployeeException when no have employees found by department")
        void givenDeleteByDepartment_whenNoEmployeesFoundByDepartment_thenThrowEmployeeException() {

            final String expectedCauseMessage = String.format("No employees found by department %s, nobody sacked!",department.getName());

            doThrow(new DbConnectionException(expectedCauseMessage)).when(repository).deleteByDepartment(department);

            final EmployeeException e = assertThrows(EmployeeException.class,
                    () -> service.deleteByDepartment(department));

            assertNotNull(e.getCause());
            assertEquals(expectedCauseMessage, e.getCause().getMessage());

            final String expectedMessage = String.format("Error: %s", expectedCauseMessage);
            assertEquals(expectedMessage, e.getMessage());

            verify(repository).deleteByDepartment(department);
        }

        @Test
        @DisplayName("Should be return 1 when employee has been deleted by department")
        void givenDeleteByDepartment_whenEmployeeHasBeenDeleted_thenReturnOne() {
            when(repository.deleteByDepartment(department)).thenReturn(1);
            assertEquals(1, service.deleteByDepartment(department));
            verify(repository).deleteByDepartment(department);
        }

        @Test
        @DisplayName("Should be return more than 1 when employees has been deleted by department")
        void givenDeleteByDepartment_whenEmployeeHasBeenDeleted_thenReturnMoreThanOne() {

            when(repository.deleteByDepartment(department)).thenReturn(
                    new Random().nextInt(2, 5)
            );

            assertTrue(service.deleteByDepartment(department) > 1);

            verify(repository).deleteByDepartment(department);
        }
    }


}



