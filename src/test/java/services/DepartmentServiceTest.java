package services;

import criteria.DepartmentFilter;
import dtos.request.DepartmentRequest;
import dtos.response.DepartmentResponse;
import exceptions.DepartmentException;
import mappers.DepartmentMapper;
import model.Department;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import repositories.interfaces.DepartmentRepository;
import utils.FormatterUtils;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQuery;
import java.util.*;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceTest {

    @Mock
    private DepartmentRepository repository;

    @Mock
    private DepartmentMapper mapper;

    @InjectMocks
    private DepartmentService service;

    @Nested
    @DisplayName("*** FindAll tests ***")
    class FindAllTests {

        @Test
        @DisplayName("Should be return DepartmentList when has departments")
        void givenFindAll_whenHasDepartment_thenReturnDepartmentList() {

            when(repository.findAll()).thenReturn(List.of(new Department()));

            assertEquals(1, repository.findAll().size());

            verify(repository).findAll();
        }

        @Test
        @DisplayName("Should be return Empty List when no has departments")
        void givenFindAll_whenNoHasDepartment_thenReturnEmptyList() {

            when(repository.findAll()).thenReturn(List.of());

            final DepartmentException e = assertThrows(DepartmentException.class, () ->
                    repository.findAll());

            final String expected = "No departments in the database!";

            assertEquals(expected, e.getMessage());

            verify(repository).findAll();
        }

    }

    @Nested
    @DisplayName("*** Save tests ***")
    class SaveTests {

        private DepartmentRequest request = new DepartmentRequest("asas");

        @Test
        @DisplayName("Should be return Department mapped to DeparmentResponse when Department is saved")
        void givenSave_whenDepartmentIsSaved_thenReturnDepartmentMappedToDepartmentResponse() {

            when(mapper.requestToDepartment(request)).thenReturn(new Department(request.name()));

            doAnswer((argument) -> {
                Department departmentSaved = argument.getArgument(0);
                departmentSaved.setId(1L);
                return null;

            }).when(repository).save(any());

            doAnswer((argument) -> {
                Department department = argument.getArgument(0);

                DepartmentResponse build = DepartmentResponse.builder()
                        .name(department.getName())
                        .createdDate(FormatterUtils.formatTemporalToString(LocalDateTime.now()))
                        .lastUpdate(FormatterUtils.formatTemporalToString(department.getLastUpdateDate()))
                        .employees(department.getEmployees())
                        .build();

                System.out.println(build);
                return build;

            }).when(mapper).departmentToResponse(any());

            assertNotNull(service.save(request));

            verify(mapper).requestToDepartment(any());
            verify(mapper).departmentToResponse(any());
            verify(repository).save(any());

        }

        @Test
        @DisplayName("Should be throw DepartmentExcetion when Department already exists")
        void givenSave_whenDepartmentAlreadyExists_thenThrowDepartmentException() {

            when(mapper.requestToDepartment(request)).thenReturn(new Department(request.name()));

            doThrow(ConstraintViolationException.class).when(repository).save(any());

            final DepartmentException e = assertThrows(DepartmentException.class, () ->
                    service.save(request));

            final String expected = format("Department %s already exists!", request.name());

            assertEquals(expected, e.getMessage());
            assertInstanceOf(ConstraintViolationException.class, e.getCause());

            verify(mapper).requestToDepartment(request);
            verify(repository).save(any());
            verify(mapper, never()).departmentToResponse(any());
        }

    }

    @Nested
    @DisplayName("*** FindByFilters tests")
    class FindByFiltersTests {

        private DepartmentFilter filters = new DepartmentFilter();

        @Test
        @DisplayName("Should be throw NullPointerException when filters is null")
        void givenFindByFilters_whenDepartmentFiltersIsNull_thenThrowNullPointerException() {

            final NullPointerException e = assertThrows(NullPointerException.class, () ->
                    service.findByFilters(null));

            final String expected = "No filters to find departments!";
            assertEquals(expected, e.getMessage());

            verify(repository, never()).findbyFilters(any());
            verify(mapper, never()).departmentToResponse(any());

        }

        @Test
        @DisplayName("Should be throw DepartmentException when departments not found by filters")
        void givenFindByFilters_whenDepartmentsNotFoundByFilters_thenThrowDepartmentException() {

            when(repository.findbyFilters(filters)).thenReturn(Set.of());

            final DepartmentException e = assertThrows(DepartmentException.class, () ->
                    service.findByFilters(filters));

            final String expected = "Departments not found by filters!";
            assertEquals(expected, e.getMessage());

            verify(repository).findbyFilters(filters);
            verify(mapper, never()).departmentToResponse(any());

        }

        @Test
        @DisplayName("Should be return Set of departments response when departments has been found by filters")
        void givenFindByFilters_whenDepartmentsHasBeenFound_thenReturnSetOfDepartmentMappedToDepartmentResponse() {

            filters.setDepartmentName("asas");

            when(repository.findbyFilters(filters)).thenReturn(Set.of(new Department(filters.getDepartmentName())));

            when(mapper.departmentToResponse(any())).thenReturn(
                    DepartmentResponse.builder()
                            .name(filters.getDepartmentName())
                            .build()
            );

            final Set<DepartmentResponse> departments = service.findByFilters(filters);

            assertEquals(1, departments.size());

            assertTrue(new ArrayList<>(departments).get(0)
                    .name()
                    .contains(filters.getDepartmentName()));

            verify(repository).findbyFilters(filters);
            verify(mapper).departmentToResponse(any());

        }

    }

    @Nested
    @DisplayName("*** FindAndDelete tests ***")
    class FindAndDeleteTests {

        private String name = "asas";

        @Test
        @DisplayName("Should be return department mapped to department reponse whent department has been found")
        void givenFindAndDelete_whenDepartmentHasBeenFound_thenReturnDepartmentMappedToDepartmentResponse() {

            when(repository.findAndDelete(name)).thenReturn(
                    Optional.of(new Department(name))
            );

            when(mapper.departmentToResponse(any())).thenReturn(
                    DepartmentResponse.builder()
                            .name(name)
                            .build()
            );

            final DepartmentResponse response = service.findAndDelete(name);

            assertEquals(name, response.name());

            verify(repository).findAndDelete(name);
            verify(mapper).departmentToResponse(any());
        }

        @Test
        @DisplayName("Should be throw DepartmentException when department not found")
        void givenFindAndDelete_whenDepartmentNotFound_thenThrowDepartmentException() {

            when(repository.findAndDelete(name)).thenReturn(Optional.empty());

            final DepartmentException e = assertThrows(DepartmentException.class, () ->
                    service.findAndDelete(name));

            final String expected = format("Department %s not found!", name);

            assertEquals(expected, e.getMessage());

            verify(repository).findAndDelete(name);
            verify(mapper, never()).departmentToResponse(any());
        }

    }

    @Nested
    @DisplayName("*** ValidateAndFormatName tests ***")
    class ValidateAndFormatNameTests {

        @Test
        @DisplayName("Should be throw DepartmentException when name is short")
        void givenValidateAndFormatName_whenNameIsShort_thenThrowDepartmentException() {

            final String name = "aa";

            final DepartmentException e = assertThrows(DepartmentException.class, () ->
                    service.validateAndFormatName(name));

            final String expected = format("%s is a short name!", name);
            assertEquals(expected, e.getMessage());

        }

        @ParameterizedTest
        @ValueSource(strings = {
                "João1", "M@ria", "José ", "Ana-Clara", "Lu!s", "Emanoel#", "Chloé*", "Lucas2", "D'Ana", "Jo a o", "Gabriela@", "Mári@h", "Raul$", "Éster_", "Marc0s", "Joana+", "Pedro!", "Luciana%", "Tómas,", "Jo_ão"
        })
        @DisplayName("Should be throw DepartmentException when name contains special symbol")
        void givenValidateAndFormatName_whenNameContainsSpecialSymbol_thenThrowDepartmentException(final String name) {

            final DepartmentException e = assertThrows(DepartmentException.class, () ->
                    service.validateAndFormatName(name));

            final String expected = format("%s contains special symbol!", name);
            assertEquals(expected, e.getMessage());

        }


        @Test
        @DisplayName("Should be return formatted name when name is valid")
        void givenValidateAndFormatName_whenNameIsValid_thenReturnFormattedName() {
            assertEquals("Pedrin", service.validateAndFormatName("pedrin"));
        }

    }

    @Nested
    @DisplayName("*** ValidateAndFormatEmployeeAge tests ***")
    class ValidateAndFormatEmployeeAgeTests {

        @Test
        @DisplayName("Should be throw DepartmentException when value is not a number")
        void givenValidateAndFormatEmployee_whenValueIsNotANumber_thenThrowDepartmentException() {

            final String temp = "II";

            final DepartmentException e = assertThrows(DepartmentException.class, () ->
                    service.validateAndFormatEmployeeAge(temp));

            final String expected = format("%s is a invalid age! Only numbers", temp);

            assertEquals(expected, e.getMessage());
            assertInstanceOf(NumberFormatException.class, e.getCause());

        }

        @Test
        @DisplayName("Should be throw DepartmentException when age is less than 18 years old")
        void givenValidateAndFormatEmployee_whenAgeUnder18Years_thenThrowDepartmentException() {

            final String temp = "17";

            final DepartmentException e = assertThrows(DepartmentException.class, () ->
                    service.validateAndFormatEmployeeAge(temp));

            final String expected = "Invalid age, under 18 years old!";

            assertEquals(expected, e.getMessage());

        }

        @Test
        @DisplayName("Should be return age when the age is over 17")
        void givenValidateAndFormatEmployee_whenAgeOver17Years_thenReturnAge() {
            assertEquals(18, service.validateAndFormatEmployeeAge("18"));
        }

    }

    @Nested
    @DisplayName("*** ValidateAndFormatDate tests ***")
    class ValidateAndFormatDateTests {

        @Test
        @DisplayName("Should be return value converted to LocalDate when value is a valid LocalDate")
        void givenValidateAndFormatDate_whenValueIsAValidLocalDate_thenReturnValueConvertedToLocalDate() {

            final String value = "10/10/2010";
            final LocalDate expected = LocalDate.of(2010, 10, 10);

            assertTrue(
                    expected.isEqual(
                            service.validateAndFormatDate(value, LocalDate.class, LocalDate::from)
                    )
            );

        }

        @Test
        @DisplayName("Should be return value converted to LocalDateTime when value is a valid LocalDateTime")
        void givenValidateAndFormatDate_whenValueIsAValidLocalDateTime_thenReturnValueConvertedToLocalDateTime() {

            final String value = "10/10/2010 10:10";
            final LocalDateTime expected = LocalDateTime.of(
                    LocalDate.of(2010, 10, 10),
                    LocalTime.of(10, 10)
            );

            assertTrue(
                    expected.isEqual(
                            service.validateAndFormatDate(value, LocalDateTime.class, LocalDateTime::from)
                    )
            );

        }

        @Test
        @DisplayName("Should be return value converted to LocalTime when value is a valid LocalTime")
        void givenValidateAndFormatDate_whenValueIsAValidLocalTime_thenReturnValueConvertedToLocalTime() {

            final String value = "10:10";
            final LocalTime expected = LocalTime.of(10, 10);

            assertTrue(
                    expected.equals(
                            service.validateAndFormatDate(value, LocalTime.class, LocalTime::from)
                    )
            );

        }

        @ParameterizedTest
        @ValueSource(strings = {
                "4#-qU_qJ", "^.cssM1T", "1h]OXVTM", "B}c|v.ZA", "br4nWuZ2", "mitz31[-", "Bcv46*P3", "ep]JjYG4", "f>.G9._B", "nrI3wyFO", ".*v;.jI6", "*Hea&)il", "'Mawe21!", "S-jL4d7-", "nt><wo^X", "oa;|g3;U", "!1t2l)$>", "d%+o?QL+", "0#H2egs!", "4t;M.Ko0",
                "UZqZd[cYF[C-Q+$", "vU<]U7K_W8.1uo/", "^S4uG,kO1{FHHg8", "|F5$w!v-pBfKB'V", "_wgG'.TZFQnmG5r", "x4*la?@n]v;Gy7l", "/WTQ6t>C2[/J!Mf", "IW]?[wXE.beH'eL", "o25.#!p@>RRYYbA", "+uw^%l9hXwp;*7n", "8&U}oYd6HULla>g", "fJ&B,0bBDUvxF6>", "8IJO|B[)k:#qh=[", "!6Gd?M6<U&2uf1b", "ObV!)peNaqRa3q2", "8[J+F]%:GB,e$4w", ">-hB%'EZ;'8xV,|", "VZ0HF33?@.<J'#/", ")|s!M0IB3Xa'BhU", "r@:9z]Z}Pu2{D9p"
        })
        @DisplayName("Should be throw DepartmentException when value is invalid (out of the pattern required)")
        void givenValidateAndFormatDate_whenValueIsInvalid_thenThrowDateTimeParseException(final String value){

            final DepartmentException e = assertThrows(DepartmentException.class, () ->
                    service.validateAndFormatDate(value, Temporal.class, LocalTime::from));

            final String expected = format("%s is a invalid date/time!", value);

            assertEquals(expected, e.getMessage());
            assertInstanceOf(DateTimeParseException.class, e.getCause());

        }

    }

    //Todo update by


}