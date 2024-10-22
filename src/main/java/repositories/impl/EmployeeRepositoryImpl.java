package repositories.impl;

import criteria.EmployeeFilter;
import database.HibernateConnection;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;
import lombok.extern.log4j.Log4j2;
import model.*;
import repositories.interfaces.EmployeeRepository;

import java.time.LocalDate;
import java.time.LocalTime;
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
                            builder.upper(departmentTable.get("name")),
                            filters.getDepartmentName()
                    )
            );
        }

        if (filters.getEmployeeName() != null) {

            predicates.add(builder.equal(
                    builder.upper(root.get("name")),
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
                    filters.getEmployeeAge()
            ));

        }


        if (filters.getBirthDate() != null) {

            predicates.add(builder.equal(
                    root.get("birthDate"),
                    filters.getBirthDate()
            ));

        }

        //Receives function necessary to compare root column with the temporal filter (date, time or datetime)
        Expression<? extends Temporal> convertedTemporal;

        if (filters.getHireDate() != null) {

            convertedTemporal = convertTemporal(builder, "date", LocalDate.class, root.get("hireDate"));

            predicates.add(builder.equal(
                    convertedTemporal,
                    filters.getHireDate()
            ));

        }

        if (filters.getHireTime() != null) {

            convertedTemporal = convertTemporal(builder, "time", LocalTime.class, root.get("hireDate"));

            predicates.add(builder.equal(
                    convertedTemporal,
                    filters.getHireTime()
            ));

        }

        //Specific inheritance attributes
        if (filters.getWorkExperience() != null) {

            predicates.add(builder.greaterThanOrEqualTo(
                    builder.treat(root, SuperiorEmployee.class).get("workExperience"),
                    filters.getWorkExperience()
            ));

        }

        if (filters.hasFaculty()) {

            predicates.add(builder.equal(
                    builder.treat(root, NormalEmployee.class).get("hasFaculty"),
                    filters.hasFaculty()
            ));
        }

        query.select(root)
                .where(predicates.toArray(Predicate[]::new));

        return new HashSet<>(connection.getManager()
                .createQuery(query)
                .getResultList());
    }

    public <T extends Temporal> Expression<T> convertTemporal(final CriteriaBuilder builder,
                                                              final String function,
                                                              final Class<T> returnedTemporalClass,
                                                              final Expression<?>... arguments) {
        return builder.function(function, returnedTemporalClass, arguments);
    }

    @Override
    public Optional<Employee> findByName(final String name) {

        return connection.getManager()
                .createNamedQuery("Employee.findByName", Employee.class)
                .setParameter("name", name)
                .getResultStream()
                .findFirst();
    }

    @Override
    public void update(final Employee employee) {
        connection.execute(EntityManager::flush);
    }

    @Override
    public void delete(final Employee employee) {
        connection.execute(entityManager -> entityManager.remove(employee));
    }
}
