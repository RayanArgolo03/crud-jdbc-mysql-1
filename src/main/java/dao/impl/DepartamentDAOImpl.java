package dao.impl;

import dao.interfaces.DepartamentDAO;
import database.DbConnection;
import domain.departaments.Departament;
import dto.departament.DepartamentDTO;
import exceptions.DbConnectionException;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Log4j2
@NoArgsConstructor
public final class DepartamentDAOImpl implements DepartamentDAO {

    @Override
    public void save(final Departament departament) {

        log.info("Tryning to save {} \n", departament.getName());

        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = this.createQueryForSaveDepartament(c, departament);
             ResultSet rs = this.executeSaveDepartament(ps)) {

            rs.next();
            Long id = rs.getLong(1);
            departament.setId(id);

        } catch (SQLIntegrityConstraintViolationException e) {
            log.error("Departament has alredy exists!");
            throw new DbConnectionException("Departament has alredy exists!");
        } catch (SQLException e) {
            throw new DbConnectionException(e.getMessage());
        }

    }

    private ResultSet executeSaveDepartament(final PreparedStatement ps)
            throws SQLException {
        ps.execute();
        return ps.getGeneratedKeys();
    }

    private PreparedStatement createQueryForSaveDepartament(final Connection c, final Departament departament)
            throws SQLException {

        final String SAVE_DEPARTAMENT = """
                INSERT INTO departaments(name)
                VALUES (?);
                """;
        PreparedStatement ps = c.prepareStatement(SAVE_DEPARTAMENT, Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, departament.getName());
        return ps;
    }

    @Override
    public List<DepartamentDTO> findAll() {

        log.info("Tryning to find departaments.. \n");

        final List<DepartamentDTO> departaments = new ArrayList<>();
        try (Connection c = DbConnection.getConnection();
             ResultSet rs = this.executeFindAll(c)) {

            while (rs.next()) departaments.add(createDepartamentDTO(rs));

        } catch (SQLException e) {
            throw new DbConnectionException(e.getMessage());
        }

        return departaments;
    }

    private DepartamentDTO createDepartamentDTO(final ResultSet rs) throws SQLException {

        final Long id = rs.getLong("id");
        final String name = rs.getString("name");
        final LocalDateTime creationDate = rs.getObject("creation_date", LocalDateTime.class);
        final LocalDateTime lastUpdateDate = rs.getObject("last_update_date", LocalDateTime.class);

        return DepartamentDTO.builder()
                .id(id)
                .name(name)
                .creationDate(creationDate)
                .lastUpdateDate(lastUpdateDate)
                .build();
    }

    private ResultSet executeFindAll(final Connection c)
            throws SQLException {

        return c.createStatement()
                .executeQuery("SELECT * FROM departaments AS d");
    }

    @Override
    public Optional<DepartamentDTO> findById(final long id) {

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
                SELECT * FROM departaments WHERE id = ?
                """;
        PreparedStatement ps = c.prepareStatement(FIND_BY_ID);
        ps.setLong(1, id);
        return ps;
    }

    @Override
    public List<DepartamentDTO> findByName(final String name) {

        log.info("Tryning to find departaments with the name {} \n", name);

        final List<DepartamentDTO> list = new ArrayList<>();
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
                SELECT * FROM departaments WHERE name LIKE ?;
                """;

        PreparedStatement ps = c.prepareStatement(FIND_BY_NAME);
        ps.setString(1, "%" + name + "%");
        return ps;
    }

    @Override
    public List<DepartamentDTO> findbyCreationDate(final LocalDate creationDateWithoutTime) {

        log.info("Tryning to find departaments with the creation date {} \n", creationDateWithoutTime);

        final List<DepartamentDTO> list = new ArrayList<>();
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
                SELECT * FROM departaments WHERE DATE(creation_date) = ?;
                """;

        PreparedStatement ps = c.prepareStatement(FIND_BY_NAME);
        ps.setDate(1, Date.valueOf(creationDateWithoutTime));
        return ps;
    }

    @Override
    public void updateName(final Departament departament, final String newName) {

        log.info("Tryning to update departament with the new name {} \n", newName);

        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = this.createQueryForUpdateName(c, newName, departament.getId())) {

            if (ps.executeUpdate() == 0) throw new DbConnectionException("No updated departaments!");

        } catch (SQLIntegrityConstraintViolationException e) {
            throw new DbConnectionException("Departament has alredy exists!");
        } catch (SQLException e) {
            throw new DbConnectionException(e.getMessage());
        }

    }

    private PreparedStatement createQueryForUpdateName(final Connection c,
                                                       final String newName,
                                                       final long id)
            throws SQLException {

        final String UPDATE_BY_NAME = """
                UPDATE departaments
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
    public int deleteById(final long id) {

        log.info("Tryning to delete departament and your employees by id {} \n", id);

        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps0 = this.createQueryForDeleteAssociatedEmployeesById(c, id);
             PreparedStatement ps1 = this.createQueryForDeleteById(c, id)) {

            //Commit delete in departament id delete method
            c.setAutoCommit(false);
            int rowsDeleted = ps0.executeUpdate();

            if (ps1.executeUpdate() == 0) throw new DbConnectionException("Error in delete by id!");
            c.commit();

            return rowsDeleted;

        } catch (SQLException e) {
            throw new DbConnectionException(e.getMessage());
        }

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
                DELETE FROM departaments
                WHERE id = ?
                """;

        PreparedStatement ps = c.prepareStatement(DELETE_BY_ID);
        ps.setLong(1, id);
        return ps;
    }

    @Override
    public int deleteByName(String name) {

        log.info("Tryning to delete departament and your employees by name {} \n", name);

        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps0 = this.createQueryForDeleteAssociatedEmployeesByName(c, name);
             PreparedStatement ps1 = this.createQueryForDeleteByName(c, name)) {

            //Commit delete in departament id delete method
            c.setAutoCommit(false);
            int rowsDeleted = ps0.executeUpdate();

            if (ps1.executeUpdate() == 0) throw new DbConnectionException("Error in delete by name!");
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
                DELETE FROM departaments
                WHERE name = ?
                """;

        PreparedStatement ps = c.prepareStatement(DELETE_BY_ID);
        ps.setString(1, name);
        return ps;
    }

    private DepartamentDTO buildDepartamentDTO(final ResultSet rs)
            throws SQLException {
        return DepartamentDTO.builder()
                .id(rs.getLong("id"))
                .name(rs.getString("name"))
                .creationDate(rs.getObject("creation_date", LocalDateTime.class))
                .lastUpdateDate(rs.getObject("last_update_date", LocalDateTime.class))
                .build();

    }
}
