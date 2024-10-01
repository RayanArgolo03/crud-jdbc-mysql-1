package repositories.impl;

import criteria.DepartmentFilter;
import database.HibernateConnection;
import jakarta.persistence.criteria.*;
import lombok.extern.log4j.Log4j2;
import model.department.Department;
import model.employee.Employee;
import repositories.interfaces.DepartmentRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.Temporal;
import java.util.*;
import java.util.function.BiFunction;

@Log4j2
public final class DepartmentRepositoryImpl implements DepartmentRepository {

    private final HibernateConnection connection;

    public DepartmentRepositoryImpl(HibernateConnection connection) {
        this.connection = connection;
    }

    @Override
    public void save(final Department department) {
        log.info("Tryning to save {} \n", department.getName());
        connection.execute((manager) -> manager.persist(department));
    }

    @Override
    public Set<Department> findAll() {

        log.info("Tryning to find departments.. \n");

        return new HashSet<>(connection.getManager()
                .createNamedQuery("Department.findAll", Department.class)
                .getResultList());
    }

    @Override
    public Set<Department> findbyFilters(final DepartmentFilter filters) {

        final CriteriaBuilder builder = connection.getManager().getCriteriaBuilder();
        final CriteriaQuery<Department> query = builder.createQuery(Department.class);
        final Root<Department> root = query.from(Department.class);

        final List<Predicate> predicates = new ArrayList<>();

        if (filters.getDepartmentName() != null) {
            predicates.add(
                    builder.equal(builder.upper(root.get("name")), filters.getDepartmentName())
            );
        }

        //Join to get Employee´s table attributes, instance if null
        Join<Department, Employee> employees = null;

        if (filters.getEmployeeName() != null) {

            employees = root.join("employees", JoinType.LEFT);
            predicates.add(
                    builder.equal(builder.upper(employees.get("name")), filters.getEmployeeName())
            );
        }

        if (filters.getEmployeeAge() != null) {

            if (employees == null) employees = root.join("employees", JoinType.LEFT);
            predicates.add(builder.equal(
                    employees.get("age"),
                    filters.getEmployeeAge())
            );
        }

        //Date function MYSQL: receiving temporal class, column in table and return criteria expression to compare (date converted)
        BiFunction<Class<? extends Temporal>, Expression<String>, Expression<? extends Temporal>> function = convertByFunction(builder, "date");

        if (filters.getEmployeeHireDate() != null) {

            if (employees == null) employees = root.join("employees", JoinType.LEFT);

            predicates.add(builder.equal(
                    function.apply(LocalDate.class, employees.get("hireDate")),
                    filters.getEmployeeHireDate()
            ));
        }

        if (filters.getCreationDate() != null) {

            predicates.add(builder.equal(
                    function.apply(LocalDate.class, root.get("createdDate")),
                    filters.getCreationDate()
            ));
        }

        if (filters.getLastUpdateDate() != null) {

            predicates.add(builder.equal(
                    function.apply(LocalDateTime.class, root.get("lastUpdateDate")),
                    filters.getLastUpdateDate()
            ));
        }

        if (filters.getLastUpdateTime() != null) {

            function = convertByFunction(builder, "time");

            predicates.add(builder.equal(
                    function.apply(LocalTime.class, root.get("lastUpdateDate")),
                    filters.getLastUpdateTime()
            ));
        }

        query.select(root)
                .where(predicates);

        return new HashSet<>(connection.getManager()
                .createQuery(query)
                .getResultList());
    }

    private BiFunction<Class<? extends Temporal>, Expression<String>, Expression<? extends Temporal>> convertByFunction(final CriteriaBuilder builder, final String function) {
        return (classs, rootAttribute) -> builder.function(function, classs, rootAttribute);
    }

    @Override
    public Optional<Department> findByDepartmentName(final String departmentName) {

        return connection.getManager()
                .createQuery("""
                        SELECT d
                        FROM Department d
                        WHERE UPPER(d.name) = :departmentName
                        """, Department.class)
                .setParameter("departmentName", departmentName)
                .getResultStream()
                .findFirst();

    }

    @Override
    public Optional<Department> findByCreationDate(final LocalDate creationDate) {

        return connection.getManager()
                .createQuery("""
                        SELECT d
                        FROM Department d
                        WHERE DATE(createdDate) = :creationDate
                        """, Department.class)
                .setParameter("creationDate", creationDate)
                .getResultStream()
                .findFirst();

    }

    @Override
    public Optional<Department> findByUpdateDate(final LocalDateTime updateDate) {

        return connection.getManager()
                .createQuery("""
                        SELECT d
                        FROM Department d
                        WHERE lastUpdateDate = :updateDate
                        """, Department.class)
                .setParameter("updateDate", updateDate)
                .getResultStream()
                .findFirst();

    }

    @Override
    public Optional<Department> findByUpdateTime(final LocalTime updateTime) {

        return connection.getManager()
                .createQuery("""
                        SELECT d
                        FROM Department d
                        WHERE TIME(lastUpdateDate) = :updateTime
                        """, Department.class)
                .setParameter("updateTime", updateTime)
                .getResultStream()
                .findFirst();

    }

    @Override
    public Optional<Department> findByEmployeeName(final String employeeName) {

        return connection.getManager()
                .createQuery("""
                        SELECT d
                        FROM Department d
                        LEFT JOIN d.employees e
                        WHERE UPPER(e.name) = :employeeName
                        """, Department.class)
                .setParameter("employeeName", employeeName)
                .getResultStream()
                .findFirst();

    }

    @Override
    public Optional<Department> findByEmployeeAge(final Integer employeeAge) {

        return connection.getManager()
                .createQuery("""
                        SELECT d
                        FROM Department d
                        LEFT JOIN d.employees e
                        WHERE e.age = :employeeAge
                        """, Department.class)
                .setParameter("employeeAge", employeeAge)
                .getResultStream()
                .findFirst();

    }

    @Override
    public Optional<Department> findByEmployeeHireDate(final LocalDate employeHireDate) {

        return connection.getManager()
                .createQuery("""
                        SELECT d
                        FROM Department d
                        LEFT JOIN d.employees e
                        WHERE DATE(e.hireDate) = :employeHireDate
                        """, Department.class)
                .setParameter("employeHireDate", employeHireDate)
                .getResultStream()
                .findFirst();

    }

    @Override
    public Department updateName(final Department department, final String newName) {
        connection.execute((manager) -> department.setName(newName));
        return department;
    }


    @Override
    public Optional<Department> findAndDelete(final String name) {

        final Optional<Department> department = connection.getManager()
                .createQuery("""
                        SELECT d FROM Department d
                        LEFT JOIN FETCH d.employees e
                        WHERE d.name = :name
                        """, Department.class)
                .setParameter("name", name)
                .getResultStream()
                .findFirst();

        department.ifPresent(departmentFound -> {
            connection.execute((manager) -> manager.remove(departmentFound));
        });

        return department;
    }
}
