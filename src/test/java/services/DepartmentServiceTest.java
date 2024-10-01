package services;

import model.department.Department;
import model.employee.NormalEmployee;
import dtos.response.DepartmentResponse;
import exceptions.DbConnectionException;
import exceptions.DepartmentException;
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
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import repositories.interfaces.DepartmentRepository;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class DepartmentServiceTest {

    @Mock
    private DepartmentRepository repository;
    @Mock
    private Mapper<DepartmentResponse, Department> mapper;

    @InjectMocks
    private DepartmentService service;

    @Nested
    @DisplayName("** Validate and format departmentName methods **")
    class ValidateAndFormatNameTests {
        @Test
        @DisplayName("Should be throw NullPointerException when the departmentName is null")
        void givenValidateAndFormatName_whenTheNameIsNull_thenThrowNPEException() {

            final NullPointerException e = assertThrows(NullPointerException.class,
                    () -> service.validateAndFormatName(null));

            final String expectedMessage = "Name can´t be null!";
            assertEquals(expectedMessage, e.getMessage());
        }

        @Test
        @DisplayName("Should be return formatted departmentName when the departmentName is not null")
        void givenValidateAndFormatName_whenTheNameIsNotNull_thenReturnFormattedName() {
            final String unformattedName = "AnYYnAme";
            final String expectedFormattedName = "Anyyname";
            assertEquals(expectedFormattedName, service.validateAndFormatName(unformattedName));
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

            final String expectedMessage = "Creation date can´t be null";
            assertEquals(expectedMessage, e.getMessage());
        }

        @ParameterizedTest
        @ValueSource(strings = {"a", "13-13-2099", "31-12-2020", "2020/12/31", "12/31/2020", "31/12/20", "32/01/2020", "31/13/2020", "00/12/2020", "15/00/2020", "ab/cd/efgh", "31/Dec/2020", " 31/12/2020", "31/12/2020 ", "31/ 12/2020", "31/12/2020a"})
        @DisplayName("Should be throw DepartmentException when the date in string does not match the pattern dd/MM/yyyy")
        void givenParseAndValidateDate_whenDateInStringNotMatchesThePattern_thenThrowDepartmentException(final String dateInString) {

            assertNotNull(dateInString);

            final DepartmentException e = assertThrows(DepartmentException.class,
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
    @DisplayName("** Save department methods **")
    class SaveDepartmentTests {

        private Department department;

        @BeforeEach
        void setUp() {
            //Without id
            department = Department.builder().name("ANy").build();
        }

        @Test
        @DisplayName("Should be throw NullPointerException when the department is null")
        void givenSaveDepartment_whenTheDepartmentIsNull_thenThrowNPEException() {

            final NullPointerException e = assertThrows(NullPointerException.class,
                    () -> service.save(null));

            final String expectedMessage = "Department can´t be null!";
            assertEquals(expectedMessage, e.getMessage());
        }

        @Test
        @DisplayName("Should be set id in department when the department is saved")
        void givenSaveDepartment_whenTheDepartmentIsSaved_thenSetId() {

            final Long expectedId = 1L;

            doAnswer(param -> {
                        Department departmentSaved = param.getArgument(0);
                        departmentSaved.setId(expectedId);
                        return null;
                    }
            ).when(repository).save(department);

            assertNull(department.getId());
            service.save(department);
            assertEquals(expectedId, department.getId());

            verify(repository).save(department);

        }

        @Test
        @DisplayName("Should be throw Department Exception when the department already exists")
        void givenSaveDepartment_whenTheDepartmentAlreadyExists_thenThrowDepartmentException() {

            final String expectedCauseMessage = String.format("Department %s already exists!", department.getName());
            doThrow(new DbConnectionException(expectedCauseMessage)).when(repository).save(department);

            final DepartmentException e = assertThrows(DepartmentException.class,
                    () -> service.save(department));

            assertNotNull(e.getCause());
            assertEquals(expectedCauseMessage, e.getCause().getMessage());

            final String expectedMessage = String.format("Error: %s", expectedCauseMessage);
            assertEquals(expectedMessage, e.getMessage());

            verify(repository).save(department);

        }

    }

    @Nested
    class FindAllMethods {
        @Test
        @DisplayName("Should be throw DepartmentException when no has departments in the database")
        void givenFindAll_whenNoHasDepartments_thenThrowDepartmentException() {

            when(repository.findAll()).thenReturn(List.of());

            final DepartmentException e = assertThrows(DepartmentException.class,
                    () -> service.findAll());

            final String expectedMessage = "No has departments!";
            assertEquals(expectedMessage, e.getMessage());

            verify(repository).findAll();
        }

        @Test
        @DisplayName("Should be return list of Mapped Departments when has departments in the database")
        void givenFindAll_whenHasDepartmentsInTheDatabase_thenReturnMappedList() {

            final DepartmentResponse d1 = DepartmentResponse.builder().build();
            final DepartmentResponse d2 = DepartmentResponse.builder().build();

            when(repository.findAll()).thenReturn(List.of(d1, d2));
            when(mapper.dtoToEntity(any())).thenReturn(Department.builder().build());

            final List<Department> departments = service.findAll();

            assertEquals(2, departments.size());
            assertInstanceOf(Department.class, departments.get(0));

            verify(repository).findAll();
            verify(mapper, times(2)).dtoToEntity(any());

        }
    }

    @DisplayName("** Find by id **")
    @Nested
    class FindByIdTests {

        private long id;

        @BeforeEach
        void setUp() {
            id = 1L;
        }

        @Test
        @DisplayName("Should be throw Department Exception when the department not found")
        void givenFindById_whenDepartmentNotFound_thenThrowDepartmentException() {

            when(repository.findById(id)).thenReturn(Optional.empty());

            final DepartmentException e = assertThrows(DepartmentException.class,
                    () -> service.findById(id));

            final String expectedMessage = String.format("Department with id %d not found!", id);
            assertEquals(expectedMessage, e.getMessage());

            verify(repository).findById(id);

        }

        @Test
        @DisplayName("Should be return a department mapped when the id is found")
        void givenFindById_whenDepartmentIsFound_thenReturnDepartmentMapped() {

            final DepartmentResponse dto = DepartmentResponse.builder().id(id).build();
            final Department expectedDepartment = Department.builder().id(dto.getId()).build();

            when(repository.findById(id)).thenReturn(Optional.of(dto));
            when(mapper.dtoToEntity(dto)).thenReturn(expectedDepartment);

            assertEquals(expectedDepartment, service.findById(id));

            verify(repository).findById(id);
            verify(mapper).dtoToEntity(dto);
        }

    }

    @DisplayName("** Find by departmentName **")
    @Nested
    class FindByNameTests {

        private String name;

        @BeforeEach
        void setUp() {
            name = "Any";
        }

        @Test
        @DisplayName("Should be throw NullPointerException when the departmentName is null")
        void givenFindByName_whenNameIsNull_thenThrowNPEException() {

            final NullPointerException e = assertThrows(NullPointerException.class,
                    () -> service.findByName(null));

            final String expectedMessage = "Name can´t be null!";
            assertEquals(expectedMessage, e.getMessage());
        }

        @Test
        @DisplayName("Should be throw DepartmentException when departments not found by departmentName passed")
        void givenFindByName_whenDepartmentsNotFoundByName_thenThrowDepartmentException() {

            when(repository.findByDepartmentName(name)).thenReturn(List.of());

            final DepartmentException e = assertThrows(DepartmentException.class,
                    () -> service.findByName(name));

            final String expectedMessage = String.format("Departments not found by departmentName %s!", name);
            assertEquals(expectedMessage, e.getMessage());

            verify(repository).findByDepartmentName(name);
        }

        @Test
        @DisplayName("Should be return a list with mapped departments when the departmentName passed found departments")
        void givenFindByName_whenFoundDepartments_thenReturnMappedDepartmentsList() {

            final DepartmentResponse dto1 = DepartmentResponse.builder().name(name + "a").build();
            final DepartmentResponse dto2 = DepartmentResponse.builder().name(name + "b").build();

            when(repository.findByDepartmentName(name)).thenReturn(List.of(dto1, dto2));

            doAnswer(param -> {
                DepartmentResponse dtoFound = param.getArgument(0);
                return Department.builder()
                        .name(dtoFound.departmentName())
                        .build();
            }).when(mapper).dtoToEntity(any());

            final List<Department> list = service.findByName(name);

            assertNotNull(list);
            assertEquals(2, list.size());
            assertTrue(list.get(0).getName().contains(name));
            assertTrue(list.get(1).getName().contains(name));

            verify(repository).findByDepartmentName(name);
            verify(mapper, atLeast(2)).dtoToEntity(any());
        }

    }

    @DisplayName("** Find by creation date **")
    @Nested
    class FindByCreationDateTests {
        private LocalDate creationDateWithoutTime;

        @BeforeEach
        void setUp() {
            creationDateWithoutTime = LocalDate.now();
        }

        @Test
        @DisplayName("Should be throw DepartmentException when departments not found by creation date passed")
        void givenFindByCreationDate_whenDepartmentsNotFoundByCreationDate_thenThrowDepartmentException() {

            when(repository.findbyCreationDate(creationDateWithoutTime)).thenReturn(Collections.emptyList());

            final DepartmentException e = assertThrows(DepartmentException.class,
                    () -> service.findByCreationDate(creationDateWithoutTime));

            final String expectedMessage = "Departaments not found!";
            assertEquals(expectedMessage, e.getMessage());
        }

        @Test
        @DisplayName("Should be return a department mapped list when the departments has been found")
        void givenFindByCreationDate_whenDepartmentsFoundByCreationDate_thenReturnADepartmentMappedList() {

            final LocalDateTime expectedCreationDate = LocalDateTime.of(creationDateWithoutTime, LocalTime.now());
            final DepartmentResponse dto1 = DepartmentResponse.builder().creationDate(expectedCreationDate).build();

            when(repository.findbyCreationDate(creationDateWithoutTime)).thenReturn(List.of(dto1));
            when(mapper.dtoToEntity(dto1)).thenReturn(
                    Department.builder()
                            .creationDate(expectedCreationDate)
                            .build()
            );

            final List<Department> list = service.findByCreationDate(creationDateWithoutTime);

            assertNotNull(list);
            assertEquals(1, list.size());
            assertEquals(expectedCreationDate, list.get(0).getCreatedDate());

            verify(repository).findbyCreationDate(creationDateWithoutTime);
            verify(mapper, atMostOnce()).dtoToEntity(dto1);

        }

    }

    @Nested
    @DisplayName("** Update departmentName **")
    class UpdateName {
        private Department department;

        @BeforeEach
        void setUp() {
            department = Department.builder().name("Any").build();
        }

        @Test
        @DisplayName("Should be throw NullPointerException when the new departmentName is null")
        void givenUpdateName_whenNewNameIsNull_thenThrowNPEException() {

            final NullPointerException e = assertThrows(NullPointerException.class,
                    () -> service.updateName(department, null));

            final String expectedMessage = "New departmentName can´t be null!";
            assertEquals(expectedMessage, e.getMessage());

        }

        @Test
        @DisplayName("Should be throw EmployeeException when the new departmentName is equals to the current departmentName")
        void givenUpdateName_whenNewNameIsEqualsToCurrentName_thenThrowEmployeeException() {

            final DepartmentException e = assertThrows(DepartmentException.class,
                    () -> service.updateName(department, department.getName()));

            final String expectedMessage = "New departmentName is equals to current departmentName!";
            assertEquals(expectedMessage, e.getMessage());

        }

        @Test
        @DisplayName("Should be set new departmentName in Department when the new departmentName is valid")
        void givenUpdateName_whenNewNameIsValid_thenSetNewNameInDepartment() {

            final String expectedNewName = "Hayek";
            final LocalDateTime expectedLastUpdateDate = LocalDateTime.now();

            assertNotEquals(expectedNewName, department.getName());
            assertNull(department.getLastUpdateDate());

            try (MockedStatic<LocalDateTime> aux = mockStatic(LocalDateTime.class)) {

                aux.when(LocalDateTime::now).thenReturn(expectedLastUpdateDate);

                doAnswer(input -> {
                            Department departmentUpdated = input.getArgument(0);
                            departmentUpdated.setName(expectedNewName);

                            //Updating last update
                            departmentUpdated.setLastUpdateDate(expectedLastUpdateDate);
                            return null;
                        }
                ).when(repository).updateName(department, expectedNewName);

                service.updateName(department, expectedNewName);

                assertEquals(expectedNewName, department.getName());
                assertEquals(expectedLastUpdateDate, department.getLastUpdateDate());

                verify(repository).updateName(department, expectedNewName);
            }
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
        @DisplayName("Should be throw DepartmentException when department not found by id")
        void givenDeleteById_whenDepartmentNotFoundById_thenThrowDepartmentException() {

            final String expectedCauseMessage = String.format("Department not found by id %d!", id);
            doThrow(new DbConnectionException(expectedCauseMessage)).when(repository).deleteById(id);

            final DepartmentException e = assertThrows(DepartmentException.class,
                    () -> service.deleteById(id));

            assertNotNull(e.getCause());
            assertEquals(expectedCauseMessage, e.getCause().getMessage());

            final String expectedMessage = String.format("Error: %s", expectedCauseMessage);
            assertEquals(expectedMessage, e.getMessage());

            verify(repository).deleteById(id);
        }

        @Test
        @DisplayName("Should be return the employees dismissed when department has been deleted by departmentName")
        void givenDeleteById_whenDepartmentHasBeenDeleted_thenReturnDismissedEmployees() {

            final List<NormalEmployee> employeesDismissedMocked = List.of(NormalEmployee.builder().build(), NormalEmployee.builder().build());

            when(repository.deleteById(1)).thenReturn(employeesDismissedMocked.size());
            assertEquals(employeesDismissedMocked.size(), service.deleteById(id));

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
        @DisplayName("Should be throw DepartmentException when no has departments found by departmentName")
        void givenDeleteByName_whenNoHasDepartmentsFoundByName_thenThrowDepartmentException() {

            final String expectedCauseMessage = String.format("Departments not found by departmentName %s!", name);
            doThrow(new DbConnectionException(expectedCauseMessage)).when(repository).deleteByName(name);

            final DepartmentException e = assertThrows(DepartmentException.class,
                    () -> service.deleteByName(name));

            assertNotNull(e.getCause());
            assertEquals(expectedCauseMessage, e.getCause().getMessage());

            final String expectedMessage = String.format("Error: %s", expectedCauseMessage);
            assertEquals(expectedMessage, e.getMessage());

            verify(repository).deleteByName(name);
        }

        @Test
        @DisplayName("Should be return 1 when employee has been deleted by departmentName")
        void givenDeleteByName_whenOneDepartmentHasBeenDeletedByName_thenReturnOne() {
            when(repository.deleteByName(name)).thenReturn(1);
            assertEquals(1, service.deleteByName(name));
            verify(repository).deleteByName(name);
        }

        @Test
        @DisplayName("Should be return more than 1 when more than one departments has been deleted by departmentName")
        void givenDeleteByName_whenMoreThanOneDepartmentsHasBeenDeletedByName_thenReturnMoreThanOne() {

            when(repository.deleteByName(name)).thenReturn(
                    new Random().nextInt(2, 5)
            );

            assertTrue(service.deleteByName(name) > 1);

            verify(repository).deleteByName(name);
        }
    }

}
