package repositories;

import criteria.EmployeeFilter;
import database.HibernateConnection;
import enums.menu.YesOrNo;
import exceptions.DatabaseException;
import model.*;
import org.h2.jdbc.JdbcSQLIntegrityConstraintViolationException;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import repositories.impl.DepartmentRepositoryImpl;
import repositories.impl.EmployeeRepositoryImpl;
import repositories.interfaces.EmployeeRepository;

import java.math.BigDecimal;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class EmployeeRepositoryTest {

    private static final String UNIT_PERSITENCE = "h2";

    private EmployeeRepository repository;
    private Employee employee;

    @BeforeEach
    void setUp() {

        repository = new EmployeeRepositoryImpl(new HibernateConnection(UNIT_PERSITENCE));

        employee = new SuperiorEmployee.Builder<>()
                .name("ATTIA")
                .document("12179823703")
                .birthDate(LocalDate.now())
                .workExperience(12)
                .age(19)
                .build();

        DepartmentRepositoryImpl departmentRepository = new DepartmentRepositoryImpl(new HibernateConnection(UNIT_PERSITENCE));
        Department department = new Department("DEP");

        departmentRepository.save(department);
        department = departmentRepository.findByDepartmentName(department.getName())
                .get();

        employee.setJobs(Set.of(
                Job.builder()
                        .department(department)
                        .employee(employee)
                        .level(Level.JUNIOR)
                        .salary(new BigDecimal("10"))
                        .build()
        ));
    }


    @Nested
    @DisplayName("*** Save tests ***")
    class SaveTests {

        @Test
        @DisplayName("Should be set id in employee when the employee is saved")
        void givenSave_whenEmployeeIsSaved_thenSetIdInEmployee() {

            repository.save(employee);
            assertNotNull(employee.getId());

            final Job job = new ArrayList<>(employee.getJobs()).get(0);

            assertNotNull(job.getEmployee().getId());
            assertNotNull(job.getDepartment().getId());
        }

        @Test
        @DisplayName("Should be throw Database Exception when the employee already exists (document is repeated)")
        void givenSave_whenEmployeeAlreadyExists_thenThrowDatabaseException() {

            repository.save(employee);

            final DatabaseException e = assertThrows(DatabaseException.class, () ->
                    repository.save(new SuperiorEmployee.Builder<>()
                            .name("a")
                            .document(employee.getDocument())
                            .birthDate(LocalDate.now())
                            .age(12)
                            .build()));

            assertInstanceOf(SQLIntegrityConstraintViolationException.class, e.getCause());

        }

    }

    @Nested
    @DisplayName("*** Find tests ***")
    class FindTests {

        @Test
        @DisplayName("Should be return employee optional with job when employee has been found by name")
        void givenFindByName_whenEmployeeHasBeenFound_thenEmployeeOptionalWithJob() {

            repository.save(employee);

            final Optional<Employee> optional = repository.findByName(employee.getName());
            assertTrue(optional.isPresent());

            final Employee employeeFound = optional.get();
            final List<Job> jobs = new ArrayList<>(employeeFound.getJobs());

            assertEquals(1, jobs.size());
        }


        @Test
        @DisplayName("Should be return empty optional when department not found by name")
        void givenFindByName_whenEmployeeNotFound_thenReturnEmptyOptional() {
            assertTrue(repository.findByName(employee.getName()).isEmpty());
        }

        @Nested
        @DisplayName("*** FindByFilters tests ***")
        class FindByFiltersTests {

            private EmployeeFilter filters = new EmployeeFilter();

            @Test
            @DisplayName("Should be return Set of employee when employee exists by department name")
            void givenFindByFilters_whenEmployeeExistsByDepartmentName_thenReturnSetOfEmployee() {

                repository.save(employee);

                filters.setDepartmentName(new ArrayList<>(employee.getJobs()).get(0)
                        .getDepartment()
                        .getName());

                final List<Employee> employees = new ArrayList<>(repository.findByFilters(filters));

                assertEquals(1, employees.size());
                assertEquals(1, employees.get(0).getJobs().size());
                assertNotNull(new ArrayList<>(employees.get(0).getJobs()).get(0).getDepartment());

            }

            @Test
            @DisplayName("Should be return empty set when employee not exists by department name")
            void givenFindByFilters_whenEmployeeNotExistsByDepartmentName_thenReturnEmptySet() {

                filters.setDepartmentName(new ArrayList<>(employee.getJobs()).get(0)
                        .getDepartment()
                        .getName());

                assertEquals(Collections.EMPTY_SET, repository.findByFilters(filters));
            }


            @Test
            @DisplayName("Should be return Set of employee when employee exists by employee name")
            void givenFindByFilters_whenEmployeeExistsByEmployeeName_thenReturnSetOfEmployee() {

                repository.save(employee);
                filters.setEmployeeName(employee.getName());

                final List<Employee> employees = new ArrayList<>(repository.findByFilters(filters));

                assertEquals(1, employees.size());
                assertEquals(1, employees.get(0).getJobs().size());
                assertNotNull(new ArrayList<>(employees.get(0).getJobs()).get(0).getDepartment());

            }

            @Test
            @DisplayName("Should be return empty set when employee not exists by employee name")
            void givenFindByFilters_whenEmployeeNotExistsByEmployeeName_thenReturnEmptySet() {
                filters.setEmployeeName(employee.getName());
                assertEquals(Collections.EMPTY_SET, repository.findByFilters(filters));
            }

            @Test
            @DisplayName("Should be return Set of employee when employee exists by employee document")
            void givenFindByFilters_whenEmployeeExistsByEmployeeDocument_thenReturnSetOfEmployee() {

                repository.save(employee);
                filters.setDocument(employee.getDocument());

                final List<Employee> employees = new ArrayList<>(repository.findByFilters(filters));

                assertEquals(1, employees.size());
                assertEquals(1, employees.get(0).getJobs().size());
                assertNotNull(new ArrayList<>(employees.get(0).getJobs()).get(0).getDepartment());

            }

            @Test
            @DisplayName("Should be return empty set when employee not exists by employee document")
            void givenFindByFilters_whenEmployeeNotExistsByEmployeeDocument_thenReturnEmptySet() {
                filters.setDocument(employee.getDocument());
                assertEquals(Collections.EMPTY_SET, repository.findByFilters(filters));
            }


            @Test
            @DisplayName("Should be return Set of employee when employee exists by employee age")
            void givenFindByFilters_whenEmployeeExistsByEmployeeAge_thenReturnSetOfEmployee() {

                repository.save(employee);
                filters.setEmployeeAge(employee.getAge());

                final List<Employee> employees = new ArrayList<>(repository.findByFilters(filters));

                assertEquals(1, employees.size());
                assertEquals(1, employees.get(0).getJobs().size());
                assertNotNull(new ArrayList<>(employees.get(0).getJobs()).get(0).getDepartment());

            }

            @Test
            @DisplayName("Should be return empty set when employee not exists by employee age")
            void givenFindByFilters_whenEmployeeNotExistsByEmployeeAge_thenReturnEmptySet() {
                filters.setEmployeeAge(employee.getAge());
                assertEquals(Collections.EMPTY_SET, repository.findByFilters(filters));
            }


            @Test
            @DisplayName("Should be return Set of employee when employee exists by employee birth date")
            void givenFindByFilters_whenEmployeeExistsByEmployeeBirthDate_thenReturnSetOfEmployee() {

                repository.save(employee);

                filters.setBirthDate(employee.getBirthDate());

                final List<Employee> employees = new ArrayList<>(repository.findByFilters(filters));

                assertEquals(1, employees.size());
                assertEquals(1, employees.get(0).getJobs().size());
                assertNotNull(new ArrayList<>(employees.get(0).getJobs()).get(0).getDepartment());

            }

            @Test
            @DisplayName("Should be return empty set when employee not exists by employee birth date")
            void givenFindByFilters_whenEmployeeNotExistsByEmployeeBirthDate_thenReturnEmptySet() {
                filters.setBirthDate(employee.getBirthDate());
                assertEquals(Collections.EMPTY_SET, repository.findByFilters(filters));
            }

            @Test
            @DisplayName("Should be return Set of employee when employee exists by employee hire date")
            void givenFindByFilters_whenEmployeeExistsByEmployeeHireDate_thenReturnSetOfEmployee() {

                repository.save(employee);

                filters.setHireDate(employee.getHireDate().toLocalDate());

                final List<Employee> employees = new ArrayList<>(repository.findByFilters(filters));

                assertEquals(1, employees.size());
                assertEquals(1, employees.get(0).getJobs().size());
                assertNotNull(new ArrayList<>(employees.get(0).getJobs()).get(0).getDepartment());

            }

            @Test
            @DisplayName("Should be return empty set when employee not exists by employee hire date")
            void givenFindByFilters_whenEmployeeNotExistsByEmployeeHireDate_thenReturnEmptySet() {
                filters.setHireDate(LocalDate.now());
                assertEquals(Collections.EMPTY_SET, repository.findByFilters(filters));
            }

            @Test
            @DisplayName("Should be return Set of employee when employee exists by employee hire time")
            void givenFindByFilters_whenEmployeeExistsByEmployeeHireTime_thenReturnSetOfEmployee() {

                repository.save(employee);

                final LocalTime hireTime = LocalTime.of(
                        employee.getHireDate().getHour(),
                        employee.getHireDate().getMinute()
                );

                filters.setHireTime(hireTime);
                final List<Employee> employees = new ArrayList<>(repository.findByFilters(filters));

                assertEquals(1, employees.size());
                assertEquals(1, employees.get(0).getJobs().size());
                assertNotNull(new ArrayList<>(employees.get(0).getJobs()).get(0).getDepartment());

            }

            @Test
            @DisplayName("Should be return empty set when employee not exists by employee hire time")
            void givenFindByFilters_whenEmployeeNotExistsByEmployeeHireTime_thenReturnEmptySet() {
                filters.setHireTime(LocalTime.now());
                assertEquals(Collections.EMPTY_SET, repository.findByFilters(filters));
            }


            @Test
            @DisplayName("Should be return Set of superior employee when employee exists by employee work experience")
            void givenFindByFilters_whenEmployeeExistsByEmployeeWorkExperience_thenReturnSetOfSuperiorEmployee() {

                final int workExperience = 12;

                repository.save(new SuperiorEmployee.Builder<>()
                        .name("a")
                        .document("12121212121")
                        .birthDate(LocalDate.now())
                        .age(12)
                        .workExperience(workExperience)
                        .build());

                filters.setWorkExperience(workExperience);
                final List<Employee> employees = new ArrayList<>(repository.findByFilters(filters));

                assertEquals(1, employees.size());
                assertInstanceOf(SuperiorEmployee.class, employees.get(0));
            }

            @Test
            @DisplayName("Should be return empty set when employee not exists by employee work experience")
            void givenFindByFilters_whenEmployeeNotExistsByEmployeeWorkExperience_thenReturnEmptySet() {
                filters.setWorkExperience(12);
                assertEquals(Collections.EMPTY_SET, repository.findByFilters(filters));
            }

            ///

            @Test
            @DisplayName("Should be return Set of normal employee when employee exists by employee has faculty")
            void givenFindByFilters_whenEmployeeExistsByEmployeeHasFaculty_thenReturnSetOfNormalEmployee() {

                repository.save(new NormalEmployee.Builder<>()
                        .name("a")
                        .document("12121212121")
                        .birthDate(LocalDate.now())
                        .age(12)
                        .hasFaculty(Boolean.FALSE)
                        .build());

                filters.setHasFaculty(YesOrNo.NO);
                final List<Employee> employees = new ArrayList<>(repository.findByFilters(filters));

                assertEquals(1, employees.size());
                assertInstanceOf(NormalEmployee.class, employees.get(0));
            }

            @Test
            @DisplayName("Should be return empty set when employee not exists by employee employee has faculty")
            void givenFindByFilters_whenEmployeeNotExistsByEmployeeHasFaculty_thenReturnEmptySet() {
                filters.setHasFaculty(YesOrNo.NO);
                assertEquals(Collections.EMPTY_SET, repository.findByFilters(filters));
            }


        }


    }

    @Nested
    @DisplayName("*** Update tests ***")
    class UpdateTests {

        @Test
        @DisplayName("Not should be throw exception when employee is updated (document not exists)")
        void givenUpdate_whenEmployeeIsUpdated_thenDoesNotThrowException() {

            final String newDocument = "81881881881";
            final String newName = "Beavouir";
            final BigDecimal newSalary = new BigDecimal(2000);

            repository.save(employee);

            employee.setDocument(newDocument);
            employee.setName(newName);

            //Pointer to job in employee jobs
            final Job job = employee.getJobs().stream()
                    .findFirst()
                    .get();
            job.setSalary(newSalary);

            assertDoesNotThrow(() -> repository.update(employee));

            assertEquals(newDocument, employee.getDocument());
            assertEquals(newName, employee.getName());
            assertEquals(newSalary, job.getSalary());
        }

        @Test
        @DisplayName("Should be throw DatabaseException when employee document already exists")
        void givenUpdate_whenEmployeeAlreadyExists_thenThrowDatabaseException() {

            final String newDocument = "81881881881";

            repository.save(employee);
            repository.save(new SuperiorEmployee.Builder<>()
                    .name("ATTIA")
                    .document(newDocument)
                    .birthDate(LocalDate.now())
                    .age(19)
                    .build());

            employee = repository.findByName(employee.getName())
                    .get();

            employee.setDocument(newDocument);

            final DatabaseException e = assertThrows(DatabaseException.class, () ->
                    repository.update(employee));

            assertInstanceOf(JdbcSQLIntegrityConstraintViolationException.class, e.getCause());

        }

    }

    @Nested
    @DisplayName("*** Delete tests ***")
    class DeleteTests {

        @Test
        @DisplayName("Not should be throw exception when employee is deleted")
        void givenDeleteByName_whenEmployeeIsDeleted_thenDoesNotThrowException() {

            repository.save(employee);
            employee = repository.findByName(employee.getName()).get();

            repository.delete(employee);

            assertTrue(repository.findByName(employee.getName()).isEmpty());

        }

    }


}