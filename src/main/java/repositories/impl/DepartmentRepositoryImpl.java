package repositories.impl;

import database.DbConnection;
import dtos.DepartmentResponse;
import exceptions.DbConnectionException;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import model.department.Department;
import repositories.interfaces.DepartmentRepository;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Log4j2
@NoArgsConstructor
public final class DepartmentRepositoryImpl implements DepartmentRepository {

    @Override
    public void save(final Department department) {

        log.info("Tryning to save {} \n", department.getName());

        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = this.createQueryForSaveDepartament(c, department);
             ResultSet rs = this.executeSaveDepartament(ps)) {

            rs.next();
            Long id = rs.getLong(1);
            department.setId(id);

        } catch (SQLIntegrityConstraintViolationException e) {
            throw new DbConnectionException(String.format("Departament %s already exists!", department.getName()));
        } catch (SQLException e) {
            throw new DbConnectionException(e.getMessage());
        }

    }

    private ResultSet executeSaveDepartament(final PreparedStatement ps)
            throws SQLException {
        ps.execute();
        return ps.getGeneratedKeys();
    }

    private PreparedStatement createQueryForSaveDepartament(final Connection c, final Department department)
            throws SQLException {

        final String SAVE_DEPARTAMENT = """
                INSERT INTO departments(name)
                VALUES (?);
                """;
        PreparedStatement ps = c.prepareStatement(SAVE_DEPARTAMENT, Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, department.getName());
        return ps;
    }

    @Override
    public List<Department> findAll() {

        log.info("Tryning to find departments.. \n");

        final List<Department> departments = new ArrayList<>();

        try (Connection c = DbConnection.getConnection();
             ResultSet rs = this.executeFindAll(c)) {

//            while (rs.next()) departments.add(this.createDepartamentDTO(rs));

        } catch (SQLException e) {
            throw new DbConnectionException(e.getMessage());
        }

        return departments;
    }

    private Department createDepartamentDTO(final ResultSet rs) throws SQLException {

//        final Long id = rs.getLong("id");
//        final String departmentName = rs.getString("departmentName");
//        final LocalDateTime creationDate = rs.getObject("creation_date", LocalDateTime.class);
//        final LocalDateTime lastUpdateDate = rs.getObject("last_update_date", LocalDateTime.class);

//        return DepartmentDTO.builder()
//                .id(id)
//                .departmentName(departmentName)
//                .creationDate(creationDate)
//                .lastUpdateDate(lastUpdateDate)
//                .build();
        return null;
    }

    private ResultSet executeFindAll(final Connection c)
            throws SQLException {

        return c.createStatement()
                .executeQuery("SELECT * FROM departments AS d");
    }

    @Override
    public Optional<DepartmentResponse> findById(final long id) {

        log.info("Tryning to find departament with id {} \n", id);

        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = this.createQueryForFindById(c, id);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return Optional.of(this.buildDepartamentDTO(rs));
            }

        } catch (SQLException e) {
            throw new DbConnectionException(e.getMessage());
        }

        return Optional.empty();
    }

    private PreparedStatement createQueryForFindById(final Connection c, final long id)
            throws SQLException {

        final String FIND_BY_ID = """
                SELECT * FROM departments WHERE id = ?
                """;
        PreparedStatement ps = c.prepareStatement(FIND_BY_ID);
        ps.setLong(1, id);
        return ps;
    }

    @Override
    public List<DepartmentResponse> findByName(final String name) {

        log.info("Tryning to find departments with the departmentName {} \n", name);

        final List<DepartmentResponse> list = new ArrayList<>();
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = this.createQueryForFindByName(c, name);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(this.buildDepartamentDTO(rs));

        } catch (SQLException e) {
            throw new DbConnectionException(e.getMessage());
        }

        return list;
    }

    private PreparedStatement createQueryForFindByName(final Connection c, final String name)
            throws SQLException {

        final String FIND_BY_NAME = """
                SELECT * FROM departments WHERE name LIKE ?;
                """;

        PreparedStatement ps = c.prepareStatement(FIND_BY_NAME);
        ps.setString(1, "%" + name + "%");
        return ps;
    }

    @Override
    public List<DepartmentResponse> findbyCreationDate(final LocalDate creationDateWithoutTime) {

        log.info("Tryning to find departments with the creation date {} \n", creationDateWithoutTime);

        final List<DepartmentResponse> list = new ArrayList<>();
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = this.createQueryForFindByCreationDate(c, creationDateWithoutTime);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(this.buildDepartamentDTO(rs));

        } catch (SQLException e) {
            throw new DbConnectionException(e.getMessage());
        }

        return list;
    }

    private PreparedStatement createQueryForFindByCreationDate(final Connection c,
                                                               final LocalDate creationDateWithoutTime)
            throws SQLException {

        final String FIND_BY_NAME = """
                SELECT * FROM departments WHERE DATE(creation_date) = ?;
                """;

        PreparedStatement ps = c.prepareStatement(FIND_BY_NAME);
        ps.setDate(1, Date.valueOf(creationDateWithoutTime));
        return ps;
    }

    @Override
    public void updateName(final Department department, final String newName) {

        log.info("Tryning to update departament with the new departmentName {} \n", newName);

        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = this.createQueryForUpdateName(c, newName, department.getId())) {

            if (ps.executeUpdate() == 0) throw new DbConnectionException("No updated departments!");

            department.setName(newName);
            department.setLastUpdateDate(LocalDateTime.now());

        } catch (SQLIntegrityConstraintViolationException e) {
            throw new DbConnectionException("Departament has already exists!");
        } catch (SQLException e) {
            throw new DbConnectionException(e.getMessage());
        }

    }

    private PreparedStatement createQueryForUpdateName(final Connection c,
                                                       final String newName,
                                                       final long id)
            throws SQLException {

        final String UPDATE_BY_NAME = """
                UPDATE departments
                SET name = ?, last_update_date = NOW()
                WHERE id = ?
                """;
        PreparedStatement ps = c.prepareStatement(UPDATE_BY_NAME);
        ps.setString(1, newName);
        ps.setLong(2, id);
        return ps;
    }

    //Returns the count of dismissed employees
    @Override
    public int deleteById(final Object id) {

        log.info("Tryning to delete departament and your employees by id {} \n", id);

//        try (Connection c = DbConnection.getConnection();
//             PreparedStatement ps0 = this.createQueryForDeleteAssociatedEmployeesById(c, id);
//             PreparedStatement ps1 = this.createQueryForDeleteById(c, id)) {
//
//            c.setAutoCommit(false);
//
//            //Deleted associate employees
//            int employeesDismissed = ps0.executeUpdate();
//
//            //Delete department
//            if (ps1.executeUpdate() == 0) {
//                throw new DbConnectionException(String.format("Department not found by id %d!", id));
//            }
//
//            c.commit();
//            return employeesDismissed;
//
//        } catch (SQLException e) {
//            throw new DbConnectionException(e.getMessage());
//        }
        return 0;
    }

    //Dismissing all employees from the departament
    private PreparedStatement createQueryForDeleteAssociatedEmployeesById(final Connection c, final long id)
            throws SQLException {

        final String DELETE_ASSOCIATED_EMPLOYEES = """
                DELETE e FROM employees AS e
                INNER JOIN departaments_has_employees AS d
                ON e.id = d.id_employee
                WHERE d.id_departament = ?;
                """;

        PreparedStatement ps = c.prepareStatement(DELETE_ASSOCIATED_EMPLOYEES);
        ps.setLong(1, id);
        return ps;
    }

    private PreparedStatement createQueryForDeleteById(final Connection c, final long id)
            throws SQLException {

        final String DELETE_BY_ID = """
                DELETE FROM departments
                WHERE id = ?
                """;

        PreparedStatement ps = c.prepareStatement(DELETE_BY_ID);
        ps.setLong(1, id);
        return ps;
    }

    @Override
    public int deleteByName(String name) {

        log.info("Tryning to delete departament and your employees by departmentName {} \n", name);

        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps0 = this.createQueryForDeleteAssociatedEmployeesByName(c, name);
             PreparedStatement ps1 = this.createQueryForDeleteByName(c, name)) {

            //Commit delete in departament id delete method
            c.setAutoCommit(false);
            int rowsDeleted = ps0.executeUpdate();

            //Delete department
            if (ps1.executeUpdate() == 0) {
                throw new DbConnectionException(String.format("Departments not found by departmentName %s!", name));
            }
            c.commit();

            return rowsDeleted;

        } catch (SQLException e) {
            throw new DbConnectionException(e.getMessage());
        }

    }

    //Dismissing all employees from the departament
    private PreparedStatement createQueryForDeleteAssociatedEmployeesByName(final Connection c, final String name)
            throws SQLException {

        final String DELETE_BY_ID_AND_RETURNS_COUNT_EMPLOYEES = """
                DELETE e FROM employees AS e
                INNER JOIN departaments_has_employees AS d
                ON e.id = d.id_employee
                WHERE d.name = ? AND e.id = d.id_employee;
                """;

        PreparedStatement ps = c.prepareStatement(DELETE_BY_ID_AND_RETURNS_COUNT_EMPLOYEES);
        ps.setString(1, name);
        return ps;
    }

    private PreparedStatement createQueryForDeleteByName(final Connection c, final String name)
            throws SQLException {

        final String DELETE_BY_ID = """
                DELETE FROM departments
                WHERE name = ?
                """;

        PreparedStatement ps = c.prepareStatement(DELETE_BY_ID);
        ps.setString(1, name);
        return ps;
    }

    private DepartmentResponse buildDepartamentDTO(final ResultSet rs)
            throws SQLException {
//        return DepartmentDTO.builder()
//                .id(rs.getLong("id"))
//                .departmentName(rs.getString("departmentName"))
//                .creationDate(rs.getObject("creation_date", LocalDateTime.class))
//                .lastUpdateDate(rs.getObject("last_update_date", LocalDateTime.class))
//                .build();
        return null;
    }
}
