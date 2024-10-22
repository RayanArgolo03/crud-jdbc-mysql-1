package repositories;

import criteria.DepartmentFilter;
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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class DepartmentRepositoryTest {

    private DepartmentRepository repository = new DepartmentRepositoryImpl(new HibernateConnection("h2"));
    ;
    private Department department = new Department("DEP");

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

            final Optional<Department> optional = repository.findByUpdateDate(department.getLastUpdateDate().truncatedTo(ChronoUnit.MICROS));
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


        @Nested
        @DisplayName("*** FindByFilters tests ***")
        class FindByFiltersTests {

            private DepartmentFilter filters = new DepartmentFilter();

            @Test
            @DisplayName("Should be return Set of departmnent with jobs when department exists by department name")
            void givenFindByFilters_whenDepartmentExistsByDepartmentName_thenReturnSetOfDepartment() {

                repository.save(department);
                filters.setDepartmentName(department.getName());

                final List<Department> departments = new ArrayList<>(repository.findbyFilters(filters));

                assertEquals(1, departments.size());
                assertNotNull(departments.get(0).getJobs());
                //Possible n+1 corrected by tuple query
                assertNotNull(new ArrayList<>(departments.get(0).getJobs()).get(0).getEmployee());
            }

            @Test
            @DisplayName("Should be return Empty Set when department not exists by department name")
            void givenFindByFilters_whenDepartmentNotExistsByDepartmentName_thenReturnEmptySet() {
                filters.setDepartmentName(department.getName());
                assertEquals(Collections.EMPTY_SET, repository.findbyFilters(filters));
            }

            @Test
            @DisplayName("Should be return Set of departmnent when department exists by employee name")
            void givenFindByFilters_whenDepartmentExistsByEmployeeName_thenReturnSetOfDepartment() {

                repository.save(department);
                filters.setEmployeeName(employee.getName());

                assertEquals(1, repository.findbyFilters(filters).size());
            }

            @Test
            @DisplayName("Should be return Empty Set when department not exists by employee name")
            void givenFindByFilters_whenDepartmentNotExistsByEmployeeName_thenReturnEmptySet() {
                filters.setEmployeeName(employee.getName());
                assertEquals(Collections.EMPTY_SET, repository.findbyFilters(filters));
            }

            @Test
            @DisplayName("Should be return set of department when department exists by employee age")
            void givenFindByFilters_whenDepartmentExistsByEmployeeAge_thenReturnSetOfDepartment() {

                repository.save(department);
                filters.setEmployeeAge(employee.getAge());

                assertEquals(1, repository.findbyFilters(filters).size());
            }

            @Test
            @DisplayName("Should be return Empty set when departments not exists by employee age")
            void givenFindByFilters_whenDeparmentNotExistsByEmployeeAge_thenReturnEmptySet() {
                filters.setEmployeeAge(employee.getAge());
                assertEquals(Collections.EMPTY_SET, repository.findbyFilters(filters));
            }

            @Test
            @DisplayName("Should be return set of department when department exists by employee hire date")
            void givenFindByFilters_whenDepartmentExistsByEmployeeHireDate_thenReturnSetOfDepartment() {

                repository.save(department);
                filters.setEmployeeHireDate(employee.getHireDate().toLocalDate());

                assertEquals(1, repository.findbyFilters(filters).size());
            }

            @Test
            @DisplayName("Should be return Empty set when departments not exists by employee hire date")
            void givenFindByFilters_whenDeparmentNotExistsByEmployeeHireDate_thenReturnEmptySet() {
                filters.setEmployeeHireDate(LocalDate.now());
                assertEquals(Collections.EMPTY_SET, repository.findbyFilters(filters));
            }

            @Test
            @DisplayName("Should be return set of department when department exists by department creation date")
            void givenFindByFilters_whenDepartmentExistsByDepartmentCreationDate_thenReturnSetOfDepartment() {

                repository.save(department);
                filters.setCreationDate(department.getCreatedDate().toLocalDate());

                assertEquals(1, repository.findbyFilters(filters).size());
            }

            @Test
            @DisplayName("Should be return Empty set when departments not exists by department creation date")
            void givenFindByFilters_whenDeparmentNotExistsByDepartmentCreationDate_thenReturnEmptySet() {
                filters.setCreationDate(LocalDate.now());
                assertEquals(Collections.EMPTY_SET, repository.findbyFilters(filters));
            }

            @Test
            @DisplayName("Should be return set of department when department exists by department update date")
            void givenFindByFilters_whenDepartmentExistsByDepartmentUpdateDate_thenReturnSetOfDepartment() {

                repository.save(department);
                department = repository.findByDepartmentName(department.getName())
                        .get();

                repository.updateName(department, "DE");

                filters.setLastUpdateDate(department.getLastUpdateDate());

                assertEquals(1, repository.findbyFilters(filters).size());
            }

            @Test
            @DisplayName("Should be return Empty set when departments not exists by department update date")
            void givenFindByFilters_whenDeparmentNotExistsByDepartmentUpdateDate_thenReturnEmptySet() {
                filters.setLastUpdateDate(department.getLastUpdateDate());
                assertEquals(Collections.EMPTY_SET, repository.findbyFilters(filters));
            }

            @Test
            @DisplayName("Should be return set of department when department exists by department update time")
            void givenFindByFilters_whenDepartmentExistsByDepartmentUpdateTime_thenReturnSetOfDepartment() {

                repository.save(department);
                department = repository.findByDepartmentName(department.getName())
                        .get();
                repository.updateName(department, "DE");

                filters.setLastUpdateTime(LocalTime.of(
                        department.getLastUpdateDate().getHour(),
                        department.getLastUpdateDate().getMinute()
                ));

                assertEquals(1, repository.findbyFilters(filters).size());
            }

            @Test
            @DisplayName("Should be return Empty set when departments not exists by department update time")
            void givenFindByFilters_whenDeparmentNotExistsByDepartmentUpdateTime_thenReturnEmptySet() {
                filters.setLastUpdateTime(LocalTime.now());
                assertEquals(Collections.EMPTY_SET, repository.findbyFilters(filters));
            }

            @Test
            @DisplayName("Should be return set of department when department exists by all filters")
            void givenFindByFilters_whenDepartmentExistsByAllFilters_thenReturnSetOfDeparment() {

                repository.save(department);
                department = repository.findByDepartmentName(department.getName())
                        .get();
                repository.updateName(department, "DE");

                filters.setDepartmentName(department.getName());
                filters.setEmployeeName(employee.getName());
                filters.setEmployeeAge(employee.getAge());
                filters.setCreationDate(department.getCreatedDate().toLocalDate());
                filters.setEmployeeHireDate(employee.getHireDate().toLocalDate());
                filters.setLastUpdateDate(department.getLastUpdateDate().truncatedTo(ChronoUnit.MICROS));

                LocalTime localTime = LocalTime.of(
                        department.getLastUpdateDate().getHour(),
                        department.getLastUpdateDate().getMinute()
                );

                filters.setLastUpdateTime(localTime);

                assertEquals(1, repository.findbyFilters(filters).size());
            }

            @Test
            @DisplayName("Should be return Empty department when department not exists by all filters")
            void givenFindByFilters_whenDepartmentNotExistsByAllFilters_thenReturnEmptySet() {


                filters.setDepartmentName(department.getName());
                filters.setEmployeeName(employee.getName());
                filters.setEmployeeAge(employee.getAge());
                filters.setCreationDate(LocalDate.now());
                filters.setEmployeeHireDate(LocalDate.now());
                filters.setLastUpdateDate(LocalDateTime.now());
                filters.setLastUpdateDate(LocalDateTime.now());
                filters.setLastUpdateTime(LocalTime.of(10, 20));


                assertEquals(Collections.EMPTY_SET, repository.findbyFilters(filters));
            }
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

        @Test
        @DisplayName("Should be throw DatabaseException when department name already exists")
        void givenUpdateName_whenDepartmentNameAlreadyExists_thenThrowDatabaseException() {

            final String oldName = department.getName();
            final String newName = "PET";

            repository.save(department);
            repository.save(new Department(newName));

            final Department departmentFound = repository.findByDepartmentName(oldName).get();

            final DatabaseException e = assertThrows(DatabaseException.class, () ->
                    repository.updateName(departmentFound, newName));

            assertInstanceOf(ConstraintViolationException.class, e.getCause());


        }

    }


}