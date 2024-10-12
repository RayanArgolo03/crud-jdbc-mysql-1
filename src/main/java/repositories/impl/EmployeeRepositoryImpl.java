package repositories.impl;

import criteria.EmployeeFilter;
import database.HibernateConnection;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;
import lombok.extern.log4j.Log4j2;
import model.*;
import repositories.interfaces.EmployeeRepository;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.util.*;

@Log4j2
public final class EmployeeRepositoryImpl implements EmployeeRepository {

    private final HibernateConnection connection;

    public EmployeeRepositoryImpl(HibernateConnection hibernateConnection) {
        connection = hibernateConnection;
    }

    @Override
    public void save(final Employee employee) {
        log.info("Tryning to save {}.. \n", employee.getName());
        connection.execute((manager) -> manager.persist(employee));
    }

    @Override
    public Set<Employee> findByFilters(final EmployeeFilter filters) {

        final CriteriaBuilder builder = connection.getManager().getCriteriaBuilder();
        final CriteriaQuery<Employee> query = builder.createQuery(Employee.class);
        final Root<Employee> root = query.from(Employee.class);

        final List<Predicate> predicates = new ArrayList<>();

        if (filters.getDepartmentName() != null) {

            final Join<Job, Department> departmentTable = root.join("jobs", JoinType.LEFT)
                    .join("department", JoinType.LEFT);

            predicates.add(builder.equal(
                    builder.lower(departmentTable.get("name")),
                    departmentTable)
            );
        }

        if (filters.getEmployeeName() != null) {

            predicates.add(builder.equal(
                    root.get("name"),
                    filters.getEmployeeName()
            ));

        }

        if (filters.getDocument() != null) {

            predicates.add(builder.equal(
                    root.get("document"),
                    filters.getDocument()
            ));

        }

        if (filters.getEmployeeAge() != null) {

            predicates.add(builder.equal(
                    root.get("age"),
                    filters.getDocument()
            ));

        }


        if (filters.getBirthDate() != null) {

            predicates.add(builder.equal(
                    root.get("age"),
                    filters.getBirthDate()
            ));

        }

        if (filters.getHireDate() != null) {

            //Todo teste com muitos argumentos
            final Expression<LocalDate> convertedHireDate = convertTemporal(builder, "date", LocalDate.class, root.get("hireDate"));

            predicates.add(builder.equal(
                    convertedHireDate,
                    filters.getBirthDate()
            ));

        }

        if (filters.getHireTime() != null) {

            final Expression<LocalTime> convertedHireDate = convertTemporal(builder, "time", LocalTime.class, root.get("hireDate"));

            predicates.add(builder.equal(
                    convertedHireDate,
                    filters.getBirthDate()
            ));

        }

        //Specific inheritance attributes
        if (filters.getWorkExperience() != null) {

            final Root<SuperiorEmployee> superiorEmployee = builder.treat(root, SuperiorEmployee.class);

            predicates.add(builder.greaterThanOrEqualTo(
                    superiorEmployee.get("workExperience"),
                    filters.getWorkExperience()
            ));

        }

        if (filters.hasFaculty()) {

            final Root<NormalEmployee> normalEmployee = builder.treat(root, NormalEmployee.class);

            predicates.add(builder.greaterThanOrEqualTo(
                    normalEmployee.get("hasFaculty"),
                    filters.hasFaculty()
            ));
        }

        query.select(root)
                .where(predicates.toArray(Predicate[]::new));

        return new HashSet<>(connection.getManager()
                .createQuery(query)
                .getResultList());
    }

    @Override
    public Optional<Employee> findByName(final String name) {

        return connection.getManager()
                .createNamedQuery("Employee.findByName", Employee.class)
                .setParameter("name", name)
                .getResultStream()
                .findFirst();
    }

    @SafeVarargs
    public final <T extends Temporal> Expression<T> convertTemporal(final CriteriaBuilder builder,
                                                                    final String function,
                                                                    final Class<T> temporalClass,
                                                                    final Expression<T>... arguments) {
        return builder.function(function, temporalClass, arguments);
    }

    @Override
    public void update(final Employee employee) {
        connection.execute(EntityManager::flush);
    }

    @Override
    public void deleteByName(final String name) {
        connection.getManager()
                .createQuery("""
                        DELETE FROM Employee e
                        WHERE name = :name
                        """, Employee.class)
                .setParameter("name", name)
                .executeUpdate();
    }
}
