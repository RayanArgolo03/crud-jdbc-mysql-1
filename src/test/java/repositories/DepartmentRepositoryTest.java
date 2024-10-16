package repositories;

import database.HibernateConnection;
import exceptions.DatabaseException;
import model.*;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import repositories.impl.DepartmentRepositoryImpl;
import repositories.interfaces.DepartmentRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DepartmentRepositoryTest {


    private DepartmentRepository repository;
    private Department department;

    @BeforeEach
    void setUp() {
        repository = new DepartmentRepositoryImpl(new HibernateConnection("h2"));
        department = new Department("DEP");
    }

    @Nested
    @DisplayName("*** Save tests ***")
    class SaveTests {

        @Test
        @DisplayName("Should be set id and created date in department when department has been saved")
        void givenSave_whenDepartmentSaved_thenSetIdAndCreatedDateInDepartment() {

            repository.save(department);

            assertNotNull(department.getId());
            assertNotNull(department.getCreatedDate());
            assertNull(department.getLastUpdateDate());

        }

        @Test
        @DisplayName("Should be throw DatabaseException when the department already exists")
        void givenSave_whenDepartmentAlreadyExists_thenThrowDatabaseException() {

            repository.save(department);

            final DatabaseException e = assertThrows(DatabaseException.class, () ->
                    repository.save(new Department(department.getName())));

            assertInstanceOf(ConstraintViolationException.class, e.getCause());

        }

    }


    @Nested
    @DisplayName("*** FindAll tests ***")
    class FindAllTests {

        @Test
        @DisplayName("Should be returned list of size 1 when the departments have been found")
        void givenFindAll_whenDepartmentsHasBeenFound_thenReturnListOfDepartments() {
            repository.save(department);
            assertEquals(1, repository.findAll().size());
        }

        @Test
        @DisplayName("Should be returned list of size 1 when the departments have been found")
        void givenFindAll_whenDepartmentsNotFound_thenReturnEmptyList() {
            assertEquals(Collections.EMPTY_LIST, repository.findAll());
        }

    }

    @Nested
    @DisplayName("*** Find tests ***")
    class FindTests {

        private Employee employee;
        private Job job;

        @BeforeEach
        void setUp() {
            employee = new NormalEmployee.Builder<>()
                    .name("PETER")
                    .document("12179823703")
                    .birthDate(LocalDate.now())
                    .age(19)
                    .hasFaculty(false)
                    .build();

            job = Job.builder()
                    .employee(employee)
                    .department(department)
                    .level(Level.MID)
                    .salary(new BigDecimal("10"))
                    .build();

            department.setJobs(Set.of(job));
        }

        @Test
        @DisplayName("Should be return Department Optional when Department has been found by name")
        void givenFindByDepartmentName_whenDepartmentHasBeenFound_thenReturnDepartmentOptionalWithJobs() {

            repository.save(department);

            final Optional<Department> optional = repository.findByDepartmentName(department.getName());
            assertTrue(optional.isPresent());

            final Department departmentFound = optional.get();
            assertNotNull(departmentFound.getId());
            assertNotNull(departmentFound.getJobs());
        }

        @Test
        @DisplayName("Should be return Empty Optional when Department not found by name")
        void givenFindByDepartmentName_whenDepartmentNotFound_thenReturnEmptyOptional() {
            assertTrue(repository.findByDepartmentName(department.getName()).isEmpty());
        }


        @Test
        @DisplayName("Should be return Department Optional when Department has been found by creation date")
        void givenFindByCreationDate_whenDepartmentHasBeenFound_thenReturnDepartmentOptional() {

            repository.save(department);

            final Optional<Department> optional = repository.findByCreationDate(department.getCreatedDate().toLocalDate());
            assertTrue(optional.isPresent());

            final Department departmentFound = optional.get();
            assertNotNull(departmentFound.getId());
            assertNotNull(departmentFound.getJobs());
        }

        @Test
        @DisplayName("Should be return Empty Optional when Department not found by creation date")
        void givenFindByCreationDate_whenDepartmentNotFound_thenReturnEmptyOptional() {
            assertTrue(repository.findByCreationDate(LocalDate.now()).isEmpty());
        }

        @Test
        @DisplayName("Should be return Department Optional when Department has been found by update date")
        void givenFindByUpdateDate_whenDepartmentHasBeenFound_thenReturnDepartmentOptional() {

            repository.save(department);

            department = repository.findByDepartmentName(department.getName()).get();
            repository.updateName(department, "newName");

            final Optional<Department> optional = repository.findByUpdateDate(department.getLastUpdateDate());
            assertTrue(optional.isPresent());

        }

        @Test
        @DisplayName("Should be return Empty Optional when Department not found by update date")
        void givenFindByUpdateDate_whenDepartmentNotFound_thenReturnEmptyOptional() {
            assertTrue(repository.findByUpdateDate(department.getLastUpdateDate()).isEmpty());
        }

        @Test
        @DisplayName("Should be return Department Optional when Department has been found by update time")
        void givenFindByUpdateTime_whenDepartmentHasBeenFound_thenReturnDepartmentOptional() {

            repository.save(department);

            department = repository.findByDepartmentName(department.getName()).get();
            repository.updateName(department, "newName");

            final LocalTime updateTimeFormatted = LocalTime.of(
                    department.getLastUpdateDate().getHour(),
                    department.getLastUpdateDate().getMinute()
            );

            final Optional<Department> optional = repository.findByUpdateTime(updateTimeFormatted);
            assertTrue(optional.isPresent());

        }

        @Test
        @DisplayName("Should be return Empty Optional when Department not found by update time")
        void givenFindByUpdateTime_whenDepartmentNotFound_thenReturnEmptyOptional() {
            assertTrue(repository.findByUpdateTime(LocalTime.now()).isEmpty());
        }

        @Test
        @DisplayName("Should be return Department Optional when Department has been found by employee name")
        void givenFindByEmployeeName_whenDepartmentHasBeenFound_thenReturnDepartmentOptionalWithJobAndEmployee() {

            repository.save(department);

            final Optional<Department> optional = repository.findByEmployeeName(employee.getName());
            assertTrue(optional.isPresent());

            final Department departmentFound = optional.get();

            assertNotNull(departmentFound.getId());
            assertEquals(1, departmentFound.getJobs().size());

            assertEquals(
                    employee,
                    new ArrayList<>(departmentFound.getJobs()).get(0).getEmployee()
            );
        }

        @Test
        @DisplayName("Should be return Empty Optional when Department not found by employee name")
        void givenFindByEmployeeName_whenDepartmentNotFound_thenReturnEmptyOptional() {
            assertTrue(repository.findByEmployeeName(employee.getName()).isEmpty());
        }

        @Test
        @DisplayName("Should be return Department Optional when Department has been found by employee age")
        void givenFindByEmployeeAge_whenDepartmentHasBeenFound_thenReturnDepartmentOptionalWithJobAndEmployee() {

            repository.save(department);

            final Optional<Department> optional = repository.findByEmployeeAge(employee.getAge());
            assertTrue(optional.isPresent());

            final Department departmentFound = optional.get();

            assertNotNull(departmentFound.getId());
            assertEquals(1, departmentFound.getJobs().size());

            assertEquals(
                    employee,
                    new ArrayList<>(departmentFound.getJobs()).get(0).getEmployee()
            );
        }

        @Test
        @DisplayName("Should be return Empty Optional when Department not found by employee age")
        void givenFindByEmployeeAge_whenDepartmentNotFound_thenReturnEmptyOptional() {
            assertTrue(repository.findByEmployeeAge(employee.getAge()).isEmpty());
        }


        @Test
        @DisplayName("Should be return Department Optional when Department has been found by employee hire date")
        void givenFindByEmployeeHireDate_whenDepartmentHasBeenFound_thenReturnDepartmentOptionalWithJobAndEmployee() {

            repository.save(department);

            final Optional<Department> optional = repository.findByEmployeeHireDate(employee.getHireDate().toLocalDate());
            assertTrue(optional.isPresent());

            final Department departmentFound = optional.get();

            assertNotNull(departmentFound.getId());
            assertEquals(1, departmentFound.getJobs().size());

            assertEquals(
                    employee,
                    new ArrayList<>(departmentFound.getJobs()).get(0).getEmployee()
            );
        }

        @Test
        @DisplayName("Should be return Empty Optional when Department not found by employee hire date")
        void givenFindByEmployeeHireDate_whenDepartmentNotFound_thenReturnEmptyOptional() {
            assertTrue(repository.findByEmployeeHireDate(LocalDate.now()).isEmpty());
        }


        @Test
        @DisplayName("Should be delete and returns Department Optional when the department has been found")
        void givenFindAndDelete_whenDepartmentHasBeenFound_thenDeleteAndReturnDepartmentOptional() {

            repository.save(department);

            final Optional<Department> optional = repository.findAndDelete(DepartmentRepositoryTest.this.department.getName());
            assertTrue(optional.isPresent());

            //Verify if department has been deleted
            assertTrue(repository.findByDepartmentName(department.getName())
                    .isEmpty());

        }

        @Test
        @DisplayName("Should be returns Empty Optional when department not found")
        void givenFindAndDelete_whenDepartmentNotFound_thenReturnEmptyOptional() {
            final Optional<Department> optional = repository.findAndDelete(DepartmentRepositoryTest.this.department.getName());
            assertTrue(optional.isEmpty());
        }


    }

    @Nested
    @DisplayName("*** UpdateName tests *** ")
    class UpdateNameTests {

        @Test
        @DisplayName("Should be update department name and last update date")
        void givenUpdateName_whenDepartmentNameIsUpdated_thenSetNewNameInDepartmentAndSetLastUpdate() {

            final String oldName = department.getName();
            final String newName = "Compliance";

            repository.save(department);

            final Department departmentFound = repository.findByDepartmentName(department.getName()).get();
            repository.updateName(departmentFound, newName);

            assertEquals(newName, departmentFound.getName());
            assertNotEquals(oldName, departmentFound.getName());
            assertNotNull(departmentFound.getLastUpdateDate());

        }

    }


}