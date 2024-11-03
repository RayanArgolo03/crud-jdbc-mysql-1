package services;

import criteria.EmployeeFilter;
import dtos.response.EmployeeResponse;
import enums.menu.YesOrNo;
import exceptions.EmployeeException;
import lombok.SneakyThrows;
import mappers.EmployeeMapper;
import model.Employee;
import model.NormalEmployee;
import model.SuperiorEmployee;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import repositories.interfaces.EmployeeRepository;
import services.EmployeeService;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import static com.github.stefanbirkner.systemlambda.SystemLambda.withTextFromSystemIn;
import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository repository;

    @Mock
    private EmployeeMapper mapper;

    @InjectMocks
    private EmployeeService service;

    @Nested
    @DisplayName("*** Save tests ***")
    class SaveTests {

        private Employee employee = new NormalEmployee.Builder<>()
                .name("Averrois")
                .build();

        @Test
        @DisplayName("Should be return employee mapped to employee response when employee is saved")
        void givenSave_whenEmployeeIsSaved_thenReturnEmployeeMappedToEmployeeResponse() {

            doAnswer(argument -> {
                Employee employeeSaved = argument.getArgument(0);
                employeeSaved.setId(1L);
                return null;

            }).when(repository).save(employee);

            doAnswer(argument -> {
                        return EmployeeResponse.builder()
                                .id(argument.getArgument(0, Employee.class).getId())
                                .name(argument.getArgument(0, Employee.class).getName())
                                .build();
                    }
            ).when(mapper).employeeToResponse(employee);

            final EmployeeResponse response = service.save(employee);

            assertEquals(1, employee.getId());
            assertEquals(employee.getId(), response.id());
            assertEquals(employee.getName(), response.name());

            verify(repository).save(employee);
            verify(mapper).employeeToResponse(employee);
        }

        @Test
        @DisplayName("Should be throw EmployeeException when employee document already exists")
        void givenSave_whenEmployeeAlreadyExists_thenThrowEmployeeException() {

            doThrow(ConstraintViolationException.class).when(repository).save(employee);

            final EmployeeException e = assertThrows(EmployeeException.class, () ->
                    service.save(employee));

            final String expected = "Employee already exists!";

            assertEquals(expected, e.getMessage());
            assertInstanceOf(ConstraintViolationException.class, e.getCause());

            verify(repository).save(employee);
            verify(mapper, never()).employeeToResponse(employee);
        }

    }

    @Nested
    @DisplayName("*** DefineSpecificAtributtes tests ***")
    class DefineSpecificAtributtesTests {

        private NormalEmployee normalEmployee = (NormalEmployee) new NormalEmployee.Builder<>()
                .name("Averrois")
                .build();

        private SuperiorEmployee superiorEmployee = (SuperiorEmployee) new SuperiorEmployee.Builder<>()
                .name("Averrois")
                .age(22)
                .build();


        @Test
        @SneakyThrows
        @DisplayName("Not should be set has faculty in NormalEmployee when is a NormalEmployee and option is no")
        void givenDefineSpecificAtributtes_whenIsNormalEmployeeAndOptionIsNo_thenNotSetHasFaculty() {

            assertFalse(normalEmployee.hasFaculty());

            withTextFromSystemIn(String.valueOf(YesOrNo.NO.ordinal() + 1)).execute(() -> {
                service.defineSpecificAtributtes(normalEmployee);
            });

            assertFalse(normalEmployee.hasFaculty());
        }


        @Test
        @SneakyThrows
        @DisplayName("Should be set default work experience in SuperiorEmployee when is a SuperiorEmployee and work experience is invalid")
        void givenDefineSpecificAtributtes_whenIsSuperiorEmployeeAndWorkExperienceIsInvalid_thenNotSetWorkExperience() {

            assertEquals(1, superiorEmployee.getWorkExperience());

            withTextFromSystemIn("-1").execute(() -> {
                service.defineSpecificAtributtes(normalEmployee);
            });

            assertEquals(1, superiorEmployee.getWorkExperience());
        }
    }

    @Nested
    @DisplayName("*** ValidateAndDefineWorkExperience tests ***")
    class ValidateAndDefineWorkExperienceTests {

        private SuperiorEmployee superiorEmployee = (SuperiorEmployee) new SuperiorEmployee.Builder<>()
                .name("Averrois")
                .age(22)
                .build();

        @Test
        @DisplayName("Should be throw EmployeeException when the work experienc is less than one year")
        void givenValidateAndDefineWorkExperience_whenWorkExperienceIsLessThanOneYear_thenThrowEmployeeException() {

            final EmployeeException e = assertThrows(EmployeeException.class, () ->
                    service.validateAndDefineWorkExperience(superiorEmployee, 18, -1));

            final String expected = "Should be has work experience!";
            assertEquals(expected, e.getMessage());

        }

        @Test
        @DisplayName("Should be throw EmployeeException when the work experienc is greater than employee age")
        void givenValidateAndDefineWorkExperience_whenWorkExperienceIsGreaterThanAge_thenThrowEmployeeException() {

            final EmployeeException e = assertThrows(EmployeeException.class, () ->
                    service.validateAndDefineWorkExperience(superiorEmployee, 18, 19));

            final String expected = "Did the employee work before they were born?";
            assertEquals(expected, e.getMessage());

        }

        @Test
        @DisplayName("Should be set work experience in SuperiorEmployee when work experience is valid")
        void givenValidateAndDefineWorkExperience_whenWorkExperienceIsValid_thenSetWorkExperienceInSuperiorEmployee() {

            int workExperience = 2;

            assertEquals(1, superiorEmployee.getWorkExperience());

            service.validateAndDefineWorkExperience(superiorEmployee, 18, workExperience);
            assertEquals(workExperience, superiorEmployee.getWorkExperience());

        }

    }

    @Nested
    @DisplayName("*** FindByFilters tests ***")
    class FindByFiltersTests {

        @Test
        @DisplayName("Should be throw NullPointerException when EmployeeFilter is null")
        void givenFindByFilters_whenEmployeeFilterIsNull_thenThrowNullPointerException() {

            final NullPointerException e = assertThrows(NullPointerException.class, () ->
                    service.findByFilters(null));

            final String expected = "No filters to find employees!";
            assertEquals(expected, e.getMessage());
        }

        @Test
        @DisplayName("Should be return set of employee mapped to employee response when employees has been found by filters")
        void givenFindByFilters_whenEmployeesHasBeenFound_thenReturnSetOfEmployeeMappedToEmployeeResponse() {

            final EmployeeFilter filters = new EmployeeFilter();

            when(repository.findByFilters(filters)).thenReturn(
                    Set.of(new SuperiorEmployee.Builder<>().build())
            );

            when(mapper.employeeToResponse(any())).thenReturn(EmployeeResponse.builder().build());

            assertEquals(1, service.findByFilters(filters).size());

            verify(repository).findByFilters(filters);
            verify(mapper).employeeToResponse(any());
        }

        @Test
        @DisplayName("Should be throw EmployeeException when employees not found")
        void givenFindByFilters_whenEmployeesNotFound_thenThrowEmployeeException() {

            final EmployeeFilter filters = new EmployeeFilter();

            when(repository.findByFilters(filters)).thenReturn(Set.of());

            final EmployeeException e = assertThrows(EmployeeException.class, () ->
                    service.findByFilters(filters));

            final String expected = "Employees not found by filters!";
            assertEquals(expected, e.getMessage());

            verify(repository).findByFilters(filters);
            verify(mapper, never()).employeeToResponse(any());
        }

    }

    @Nested
    @DisplayName("*** FindByDocument tests ***")
    class FindByDocumentTests {

        @Test
        @DisplayName("Should be return employee when employee has been found by document")
        void givenFindByDocument_whenEmployeeHasBeenFound_thenReturnEmployee() {

            when(repository.findByDocument(any())).thenReturn(
                    Optional.of(new SuperiorEmployee.Builder<>().build())
            );

            assertNotNull(service.findByDocument("1212"));

            verify(repository).findByDocument(any());

        }

        @Test
        @DisplayName("Should be throw EmployeeException when employee not found by document")
        void givenFindByDocument_whenEmployeeNotFound_thenThrowEmployeeException() {

            final String document = "1212";

            when(repository.findByDocument(document)).thenReturn(
                    Optional.empty()
            );

            final EmployeeException e = assertThrows(EmployeeException.class, () ->
                    service.findByDocument(document));

            final String expected = format("Employee with document %s not found!", document);
            assertEquals(expected, e.getMessage());

            verify(repository).findByDocument(any());

        }
    }

    @Nested
    @DisplayName("*** Delete tests ***")
    class DeleteTests {

        private NormalEmployee normalEmployee = (NormalEmployee) new NormalEmployee.Builder<>()
                .name("Mill")
                .build();

        @Test
        @SneakyThrows
        @DisplayName("Not should be call repository delete method when dismissal is cancelled")
        void givenDelete_whenDeleteIsCancelled_thenDoesNotCallDeleteMethod() {

            withTextFromSystemIn(String.valueOf(YesOrNo.NO.ordinal() + 1)).execute(() -> {
                service.delete(normalEmployee);
            });

            verify(repository, never()).delete(normalEmployee);
        }


    }

    @Nested
    @DisplayName("*** ValidateAndFormatSalary tests ***")
    class ValidateAndFormatSalaryTests {

        @ParameterizedTest
        @ValueSource(strings = {
                "123,456", "12.345,67", "abc", "1,2,3", "123..45", "12,34.56", "12.3.4", "-123", "1 234", "123 456", "0..1", "1,23.45", "12,3.4", "0.12.3", ",123", ".456", "1.2.3.4", "123.", "12,345", "0,123"
        })
        @DisplayName("Should be throw EmployeeException when the value is invalid")
        void givenValidateAndFormatSalary_whenValueIsInvalidSalary_thenThrowEmployeeException(final String value) {

            final EmployeeException e = assertThrows(EmployeeException.class, () ->
                    service.validateAndFormatSalary(value));

            final String expected = format("%s is invalid salary! (Only numbers and decimal values separated by dot or comma)", value);
            assertEquals(expected, e.getMessage());

        }

        @ParameterizedTest
        @ValueSource(strings = {
                "0", "1", "123", "4567", "89.0", "10,1", "345.67", "8,90", "12.3", "45,6", "7890",

        })
        @DisplayName("Should be return BigDecimal value when the value is a valid salary")
        void givenValidateAndFormatSalary_whenValueIsValidSalary_thenReturnBigDecimalValue(final String value) {
            assertDoesNotThrow(() -> service.validateAndFormatSalary(value));
        }

    }

    @Nested
    @DisplayName("*** ValidateAndFormatName tests ***")
    class ValidateAndFormatNameTests {

        @Test
        @DisplayName("Should be throw EmployeeException when is a short name")
        void givenValidateAndFormatName_whenIsAShortName_thenThrowEmployeeException() {

            final String value = "aa";

            final EmployeeException e = assertThrows(EmployeeException.class, () ->
                    service.validateAndFormatName(value));

            final String expected = format("%s is a short name!", value);
            assertEquals(expected, e.getMessage());

        }

        @Test
        @DisplayName("Should be throw EmployeeException when the name contains special symbol")
        void givenValidateAndFormatName_wheNameContainsSpecialSymbol_thenThrowEmployeeException() {

            final String value = "aaa&";

            final EmployeeException e = assertThrows(EmployeeException.class, () ->
                    service.validateAndFormatName(value));

            final String expected = format("%s contains special characters!", value);
            assertEquals(expected, e.getMessage());

        }

        @Test
        @DisplayName("Should be return formatted name when is a valid name")
        void givenValidateAndFormatName_wheNameIsAValidName_thenReturnFormattedName() {

            final String value = "connery";
            final String expected = value.substring(0, 1).toUpperCase().concat(value.substring(1).toLowerCase());

            assertEquals(expected, service.validateAndFormatName(value));
        }

    }

    @Nested
    @DisplayName("*** ValidateAndFormatDocument tests ***")
    class ValidateAndFormatDocumentTests {

        @ParameterizedTest
        @ValueSource(strings = {
                "12345678901", "123.4567.89-01", "12.345.6789-0", "123-456-789-01", "123.456.789.012", "12.34.567-89", "123.456.78-90", "123.45.678-90", "123456789-01", "123.456.7890-1", "123.456.789-012", "ABC.DEF.GHI-JK", "123 456 789 01", "123.456.789.XX", "12.345.6789.01", "123.45A.789-01", "123.456.789-0A", "123.4567.89-0", "123..456.789-01", "123.456.78-.01"
        })
        @DisplayName("Should be throw EmployeeException when the value is invalid")
        void givenValidateAndFormatDocument_whenValueIsInvalidDocument_thenThrowEmployeeException(final String value) {

            final EmployeeException e = assertThrows(EmployeeException.class, () ->
                    service.validateAndFormatDocument(value));

            final String expected = format("CPF %s does not match the patern xxx.xxx.xxx-xx with symbols!", value);
            assertEquals(expected, e.getMessage());

        }


        @ParameterizedTest
        @ValueSource(strings = {
                "123.456.789-00", "987.654.321-99", "111.222.333-44", "555.666.777-88", "012.345.678-90", "123.123.123-12", "456.456.456-45", "789.789.789-78", "000.111.222-33", "333.444.555-66"
        })
        @DisplayName("Should be return formatted document when the value is a valid")
        void givenValidateAndFormatDocument_whenValueValidDocument_thenReturnFormattedDocument(final String value) {
            assertDoesNotThrow(() -> service.validateAndFormatDocument(value));
        }

    }

    @Nested
    @DisplayName("*** GenerateAge tests ***")
    class GenerateAgeTests {

        private LocalDate mockNow = LocalDate.of(2010, 4, 5);

        @Test
        @DisplayName("Should be throw EmployeeException when employee has less than 15 years")
        void givenGenerateAge_whenEmployeeUnderage_thenThrowEmployeeException() {

            final LocalDate birthDate = LocalDate.of(
                    mockNow.getYear() - 14,
                    mockNow.getMonthValue(),
                    mockNow.getDayOfMonth()
            );

            try (MockedStatic<LocalDate> mock = mockStatic(LocalDate.class)) {

                mock.when(LocalDate::now).thenReturn(mockNow);

                //Use of Period until method
                mock.when(() -> LocalDate.from(any())).thenReturn(mockNow);

                final EmployeeException e = assertThrows(EmployeeException.class, ()
                        -> service.generateAge(birthDate));

                final String expected = "Employee underage!";
                assertEquals(expected, e.getMessage());

            }


        }

        @Test
        @DisplayName("Should be return exactly years difference between birthdate and now when its anniversary")
        void givenGenerateAge_whenItsAnniversary_thenReturnExactlyYearsDifferenceBetweenBirthDateAndNow() {

            final int expectedAge = 18;

            final LocalDate birthDate = LocalDate.of(
                    mockNow.getYear() - expectedAge,
                    mockNow.getMonthValue(),
                    mockNow.getDayOfMonth()
            );


            try (MockedStatic<LocalDate> mock = mockStatic(LocalDate.class)) {
                mock.when(LocalDate::now).thenReturn(mockNow);

                //Use of Period until method
                mock.when(() -> LocalDate.from(any())).thenReturn(mockNow);

                assertEquals(expectedAge, service.generateAge(birthDate));
            }

        }

        @Test
        @DisplayName("Should be return exactly years difference between birthdate and now when anniversary was before")
        void givenGenerateAge_whenAnniversaryWasBefore_thenReturnExactlyYearsDifferenceBetweenBirthDateAndNow() {

            final int expectedAge = 18;

            final LocalDate birthDate = LocalDate.of(
                    mockNow.getYear() - expectedAge,
                    mockNow.getMonthValue(),
                    mockNow.getDayOfMonth() - 1);

            try (MockedStatic<LocalDate> mock = mockStatic(LocalDate.class)) {
                mock.when(LocalDate::now).thenReturn(mockNow);

                //Use of Period until method
                mock.when(() -> LocalDate.from(any())).thenReturn(mockNow);

                assertEquals(expectedAge, service.generateAge(birthDate));
            }

        }

        @Test
        @DisplayName("Should be return difference between birthdate minus one year")
        void givenGenerateAge_whenNoAnniversary_thenReturnDifferenceBetweenBirthDateAndNowMinusOneYear() {

            final int expectedAge = 18;

            final LocalDate birthDate = LocalDate.of(
                    mockNow.getYear() - expectedAge,
                    mockNow.getMonthValue() - 1,
                    mockNow.getDayOfMonth());

            try (MockedStatic<LocalDate> mock = mockStatic(LocalDate.class)) {
                mock.when(LocalDate::now).thenReturn(mockNow);

                //Use of Period until method
                mock.when(() -> LocalDate.from(any())).thenReturn(mockNow);

                assertEquals(expectedAge - 1, service.generateAge(birthDate));
            }

        }

    }

}