package dao.impl;

import dao.interfaces.EmployeeDAO;
import database.DbConnection;
import domain.departament.Departament;
import domain.departament.Level;
import domain.employee.Employee;
import domain.employee.NormalEmployee;
import domain.employee.SuperiorEmployee;
import dto.employee.EmployeeBaseDTO;
import dto.employee.NormalEmployeeDTO;
import dto.employee.SuperiorEmployeeDTO;
import exceptions.DbConnectionException;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Log4j2
@NoArgsConstructor
public final class EmployeeDAOImpl implements EmployeeDAO {
    @Override
    public void save(final Employee employee) {

        log.info("Tryning to save {}.. \n", employee.getName());

        //If error occurs, not open; if not occurs, open and close in save specific employee
        final Connection c = DbConnection.getConnection();

        try (PreparedStatement ps = this.createQueryForSaveBaseEmployee(c,
                employee.getName(),
                employee.getBirthDate(),
                employee.getAge(),
                employee.getDocument());
             ResultSet rs = this.executeSaveEmployee(c, ps)
        ) {

            rs.next();
            final Long employeeId = rs.getLong(1);
            employee.setId(employeeId);

            //Save jobs information in pivot table
            this.saveJobsInformation(c, employee.getId(), employee.getDepartamentsAndLevelsAndSalaries());
        }

        //If employee document alredy exists
        catch (SQLIntegrityConstraintViolationException e) {
            try {
                c.close();
            } catch (SQLException ee) {
                log.error(ee.getMessage());
            }

            throw new DbConnectionException("Employee alredy exists!");

        } catch (Exception e) {
            try {
                c.close();
            } catch (SQLException ee) {
                log.error(ee.getMessage());
            }

            throw new DbConnectionException(e.getMessage());
        }

    }

    private PreparedStatement createQueryForSaveBaseEmployee(final Connection c,
                                                             final String name,
                                                             final LocalDate birthDate,
                                                             final int age,
                                                             final String document)
            throws SQLException {

        final String SAVE_BASE_EMPLOYEE = """
                INSERT INTO employees (name, birth_date, age, document)
                VALUES (?, ?, ?, ?)
                """;

        PreparedStatement ps = c.prepareStatement(SAVE_BASE_EMPLOYEE, Statement.RETURN_GENERATED_KEYS);

        ps.setString(1, name);
        ps.setDate(2, Date.valueOf(birthDate));
        ps.setInt(3, age);
        ps.setString(4, document);

        return ps;
    }

    private ResultSet executeSaveEmployee(final Connection c, final PreparedStatement ps)
            throws SQLException {

        //Commit will be done in specific methods (superior and normal employee)
        c.setAutoCommit(false);

        ps.executeUpdate();

        return ps.getGeneratedKeys();
    }

    public void saveJobsInformation(final Connection c,
                                    final long employeeId,
                                    final Map<Departament, Map<Level, BigDecimal>> dls) {

        log.info("Saving jobs informations.. ");

        //Not commit here
        try (PreparedStatement ps = this.createQueryForSaveJobsInformations(c)) {

            for (Departament d : dls.keySet()) {
                Level level = dls.get(d).keySet().stream().findFirst().get();
                BigDecimal salary = dls.get(d).get(level);
                this.executeSaveJobsInformations(ps, d.getId(), employeeId, level, salary);
            }

        } catch (SQLException e) {
            throw new DbConnectionException(e.getMessage());
        }
    }

    private PreparedStatement createQueryForSaveJobsInformations(final Connection c)
            throws SQLException {

        final String SAVE_JOBS_INFORMATIONS = """
                INSERT INTO departaments_has_employees (id_departament, id_employee, level, salary)
                VALUES (?,?,?,?)
                """;

        return c.prepareStatement(SAVE_JOBS_INFORMATIONS);
    }

    private void executeSaveJobsInformations(final PreparedStatement ps,
                                             final long departamentId,
                                             final long employeeId,
                                             final Level level,
                                             final BigDecimal salary)
            throws SQLException {

        ps.setLong(1, departamentId);
        ps.setLong(2, employeeId);
        ps.setString(3, level.name());
        ps.setBigDecimal(4, salary);

        ps.execute();
    }

    @Override
    public void saveNormalEmployee(final NormalEmployee ne) {

        log.info("Tryning to save {} with your type in inheritance table \n", ne.getName());

        //Get opened connection!
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = this.createQueryForSaveNormalEmp(c, ne.getId(),
                     ne.isHasFaculty())) {

            ps.execute();

            //Commit Employee with Pivot info and specific instance
            c.commit();

        } catch (SQLException e) {
            log.error("Process failure, employee not hired!");
            throw new DbConnectionException(e.getMessage());
        }
    }

    private PreparedStatement createQueryForSaveNormalEmp(final Connection c,
                                                          final Long id,
                                                          final boolean hasFaculty)
            throws SQLException {

        final String SAVE_NORMAL_EMP = """
                INSERT INTO normal_employees (id, has_faculty)
                VALUES (?, ?)""";

        PreparedStatement ps = c.prepareStatement(SAVE_NORMAL_EMP);
        ps.setLong(1, id);
        ps.setBoolean(2, hasFaculty);
        return ps;
    }

    @Override
    public void saveSuperiorEmployee(final SuperiorEmployee se) {

        log.info("Tryning to save {} with your type in inheritance table \n", se.getName());

        //Get opened connection!
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = this.createQueryForSaveSuperiorEmp(c, se.getId(),
                     se.getWorkExperience())) {

            ps.execute();

            //Commit Employee with Pivot info and specific instance
            c.commit();

        } catch (SQLException e) {
            log.error("Process failure, employee not hired!");
            throw new DbConnectionException(e.getMessage());
        }
    }

    private PreparedStatement createQueryForSaveSuperiorEmp(final Connection c, final Long id, final int workExperience)
            throws SQLException {

        final String SAVE_NORMAL_EMP = """
                INSERT INTO superior_employees (id, work_experience)
                VALUES (?, ?)""";

        PreparedStatement ps = c.prepareStatement(SAVE_NORMAL_EMP);
        ps.setLong(1, id);
        ps.setInt(2, workExperience);
        return ps;
    }

    @Override
    public void updateName(final Employee employee, final String newName) {

        log.info("Updating name of employee {} \n", employee.getName());

        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = this.createQueryForUpdateName(c, newName, employee.getId())) {

            if (ps.executeUpdate() == 0) throw new DbConnectionException("Error in update employee name!");

        } catch (SQLException e) {
            throw new DbConnectionException(e.getMessage());
        }
    }

    private PreparedStatement createQueryForUpdateName(final Connection c, final String name, final long id)
            throws SQLException {

        final String UPDATE_BY_NAME = """
                UPDATE employees AS e
                SET e.name = ?
                WHERE e.id = ?
                """;
        PreparedStatement ps = c.prepareStatement(UPDATE_BY_NAME);
        ps.setString(1, name);
        ps.setLong(2, id);
        return ps;
    }

    @Override
    public void updateDocument(final Employee employee, final String newDocument) {

        log.info("Updating document of employee {} \n", employee.getName());

        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = this.createQueryForUpdateDocument(c, newDocument, employee.getId())) {

            if (ps.executeUpdate() == 0) {
                throw new DbConnectionException("Error in update employee document!");
            }

        }
        //Case document repeated
        catch (SQLIntegrityConstraintViolationException e) {
            throw new DbConnectionException("Employee document alredy exists!");
        } catch (SQLException e) {
            throw new DbConnectionException(e.getMessage());
        }
    }

    private PreparedStatement createQueryForUpdateDocument(final Connection c, final String document, final long id)
            throws SQLException {

        final String UPDATE_BY_NAME = """
                UPDATE employees AS e
                SET e.document = ?
                WHERE e.id = ?
                """;
        PreparedStatement ps = c.prepareStatement(UPDATE_BY_NAME);
        ps.setString(1, document);
        ps.setLong(2, id);
        return ps;
    }

    @Override
    public void updateLevel(final Employee employee, final Departament departament, final Level newLevel) {

        log.info("Updating seniority of employee {} \n", employee.getName());

        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = this.createQueryForUpdateLevel(c, departament.getId(), employee.getId(), newLevel)) {

            if (ps.executeUpdate() == 0) throw new DbConnectionException("Error in update employee level!");

        } catch (SQLException e) {
            throw new DbConnectionException(e.getMessage());
        }
    }

    private PreparedStatement createQueryForUpdateLevel(final Connection c,
                                                        final long idDepartament,
                                                        final long idEmployee,
                                                        final Level newLevel)
            throws SQLException {

        final String UPDATE_LEVEL = """
                UPDATE departaments_has_employees
                SET level = ?
                WHERE id_departament = ? AND id_employee = ?;
                """;
        PreparedStatement ps = c.prepareStatement(UPDATE_LEVEL);
        ps.setString(1, newLevel.name());
        ps.setLong(2, idDepartament);
        ps.setLong(3, idEmployee);
        return ps;
    }

    @Override
    public void updateSalary(final Employee employee, final Departament departament, final BigDecimal newSalary) {

        log.info("Updating salary of employee {} \n", employee.getName());

        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = this.createQueryForUpdateSalary(c, departament.getId(), employee.getId(), newSalary)) {

            if (ps.executeUpdate() == 0) throw new DbConnectionException("Error in update employee salary!");

        } catch (SQLException e) {
            throw new DbConnectionException(e.getMessage());
        }

    }

    private PreparedStatement createQueryForUpdateSalary(final Connection c,
                                                         final long idDepartament,
                                                         final long idEmployee,
                                                         final BigDecimal newSalary)
            throws SQLException {

        final String UPDATE_SALARY = """
                UPDATE departaments_has_employees
                SET salary = ?
                WHERE id_departament = ? AND id_employee = ?;
                """;
        PreparedStatement ps = c.prepareStatement(UPDATE_SALARY);
        ps.setBigDecimal(1, newSalary);
        ps.setLong(2, idDepartament);
        ps.setLong(3, idEmployee);
        return ps;
    }

    @Override
    public Optional<EmployeeBaseDTO> findById(final long employeeId) {

        log.info("Tryning to find by id {} \n", employeeId);

        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps0 = this.createQueryForFindBaseEmployeeById(c, employeeId);
             ResultSet rs0 = ps0.executeQuery()) {

            //Consumed the next row here, got records in specific methods
            if (rs0.next()) {
                return Optional.of(this.createEmployeeBaseDTO(c, rs0, employeeId));
            }

        } catch (SQLException e) {
            throw new DbConnectionException(e.getMessage());
        }

        return Optional.empty();
    }

    private PreparedStatement createQueryForFindBaseEmployeeById(final Connection c, final long id)
            throws SQLException {

        final String FIND_BASE_EMP = """
                SELECT
                    e.name, e.birth_date, e.age, e.document, e.hire_date
                FROM
                    employees AS e
                WHERE
                    e.id = ?
                """;

        PreparedStatement ps = c.prepareStatement(FIND_BASE_EMP);
        ps.setLong(1, id);
        return ps;
    }

    @Override
    public List<EmployeeBaseDTO> findByName(final String name) {

        log.info("Tryning to find by name {} \n", name);

        final List<EmployeeBaseDTO> list = new ArrayList<>();
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps0 = this.createQueryForFindByName(c, name);
             ResultSet rs0 = ps0.executeQuery()) {

            while (rs0.next()) {
                long id = rs0.getLong("id");
                EmployeeBaseDTO dto = this.createEmployeeBaseDTO(c, rs0, id);
                list.add(dto);
            }

        } catch (SQLException e) {
            throw new DbConnectionException(e.getMessage());
        }

        return list;
    }


    private PreparedStatement createQueryForFindByName(final Connection c, final String name)
            throws SQLException {

        final String FIND_BY_NAME = """
                    SELECT e.id, e.name, e.birth_date, e.age, e.document, e.hire_date
                    FROM employees AS e
                    WHERE e.name LIKE ?;
                """;

        PreparedStatement ps = c.prepareStatement(FIND_BY_NAME);
        ps.setString(1, "%" + name + "%");
        return ps;
    }

    @Override
    public Optional<EmployeeBaseDTO> findByDocument(final String document) {

        log.info("Tryning to find by document {} \n", document);

        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps0 = this.createQueryForFindByDocument(c, document);
             ResultSet rs0 = ps0.executeQuery()) {

            if (rs0.next()) {
                final long id = rs0.getLong("id");
                return Optional.of(this.createEmployeeBaseDTO(c, rs0, id));
            }

        } catch (SQLException e) {
            throw new DbConnectionException(e.getMessage());
        }

        return Optional.empty();
    }

    private PreparedStatement createQueryForFindByDocument(final Connection c, final String document)
            throws SQLException {

        final String FIND_BY_DOCUMENT = """
                SELECT e.id, e.name, e.birth_date, e.age, e.document, e.hire_date
                FROM employees AS e
                WHERE e.document = ?;
                """;
        PreparedStatement ps = c.prepareStatement(FIND_BY_DOCUMENT);
        ps.setString(1, document);
        return ps;
    }

    @Override
    public List<EmployeeBaseDTO> findByHireDate(final LocalDate hireDateWithoutTime) {

        log.info("Tryning to find by hire date {} \n", hireDateWithoutTime);
        final List<EmployeeBaseDTO> list = new ArrayList<>();

        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps0 = this.createQueryForFindByHireDate(c, hireDateWithoutTime);
             ResultSet rs0 = ps0.executeQuery()) {

            while (rs0.next()) {
                long id = rs0.getLong("id");
                EmployeeBaseDTO dto = this.createEmployeeBaseDTO(c, rs0, id);
                list.add(dto);
            }

        } catch (SQLException e) {
            throw new DbConnectionException(e.getMessage());
        }

        return list;
    }

    private PreparedStatement createQueryForFindByHireDate(final Connection c,
                                                           final LocalDate hireDateWithoutTime)
            throws SQLException {

        final String FIND_BY_HIRE_DATE = """
                SELECT e.id, e.name, e.birth_date, e.age, e.document, e.hire_date
                FROM employees AS e
                WHERE DATE(e.hire_date) = ?;
                """;

        PreparedStatement ps = c.prepareStatement(FIND_BY_HIRE_DATE);
        ps.setDate(1, Date.valueOf(hireDateWithoutTime));
        return ps;
    }

    @Override
    public List<EmployeeBaseDTO> findByAge(int age) {

        final List<EmployeeBaseDTO> list = new ArrayList<>();

        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps0 = this.createQueryForFindByAge(c, age);
             ResultSet rs0 = ps0.executeQuery()) {

            while (rs0.next()) {
                long id = rs0.getLong("id");
                list.add(this.createEmployeeBaseDTO(c, rs0, id));
            }

        } catch (SQLException e) {
            throw new DbConnectionException(e.getMessage());
        }

        return list;
    }

    private PreparedStatement createQueryForFindByAge(final Connection c, final int age)
            throws SQLException {

        final String FIND_BY_AGE = """
                SELECT e.id, e.name, e.birth_date, e.age, e.document, e.hire_date
                FROM employees AS e
                WHERE e.age = ?;
                """;
        PreparedStatement ps = c.prepareStatement(FIND_BY_AGE);
        ps.setInt(1, age);
        return ps;
    }

    @Override
    public int deleteById(long id) {

        log.info("Tryning to delete employee with id {} \n", id);

        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = this.createQueryForDeleteById(c, id)) {

            final int deletedRows = ps.executeUpdate();
            if (deletedRows == 0) {
                throw new DbConnectionException("No return, no employees have been sacked!");
            }

            return deletedRows;

        } catch (SQLException e) {
            throw new DbConnectionException(e.getMessage());
        }


    }

    private PreparedStatement createQueryForDeleteById(final Connection c, final long id)
            throws SQLException {

        final String DELETE_BY_ID = """
                DELETE FROM employees
                WHERE id = ?;
                """;
        PreparedStatement ps = c.prepareStatement(DELETE_BY_ID);
        ps.setLong(1, id);
        return ps;
    }


    @Override
    public int deleteByName(final String name) {

        log.info("Tryning to delete employees called {} \n", name);

        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = this.createQueryForDeleteByName(c, name)) {

            final int deletedRows = ps.executeUpdate();
            if (deletedRows == 0) {
                throw new DbConnectionException("No return, no employees have been sacked!");
            }

            return deletedRows;

        } catch (SQLException e) {
            throw new DbConnectionException(e.getMessage());
        }

    }

    private PreparedStatement createQueryForDeleteByName(final Connection c, final String name)
            throws SQLException {

        final String DELETE_BY_NAME = """
                DELETE FROM employees
                WHERE LOWER(name) = LOWER(?);
                """;

        PreparedStatement ps = c.prepareStatement(DELETE_BY_NAME);
        ps.setString(1, name);
        return ps;
    }

    @Override
    public int deleteByDocument(final String document) {

        log.info("Tryning to delete employee with a document {} \n", document);

        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = this.createQueryForDeleteByDocument(c, document)) {

            int deletedRows = ps.executeUpdate();
            if (deletedRows == 0) {
                throw new DbConnectionException("No return, no employee have been sacked!");
            }

            return deletedRows;

        } catch (SQLException e) {
            throw new DbConnectionException(e.getMessage());
        }

    }

    private PreparedStatement createQueryForDeleteByDocument(final Connection c, final String document)
            throws SQLException {

        final String DELETE_BY_DOCUMENT = """
                DELETE FROM employees
                WHERE document = ?;
                """;
        PreparedStatement ps = c.prepareStatement(DELETE_BY_DOCUMENT);
        ps.setString(1, document);
        return ps;
    }

    @Override
    public int deleteByHireDate(final LocalDate hireDateWithoutTime) {

        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = this.createQueryForDeleteHireDate(c, hireDateWithoutTime)) {

            int deletedRows = ps.executeUpdate();
            if (deletedRows == 0) {
                throw new DbConnectionException("No return, no employees have been sacked!");
            }

            return deletedRows;

        } catch (SQLException e) {
            throw new DbConnectionException(e.getMessage());
        }

    }

    private PreparedStatement createQueryForDeleteHireDate(final Connection c,
                                                           final LocalDate hireDateWithoutTime)
            throws SQLException {

        final String DELETE_BY_DOCUMENT = """
                DELETE FROM employees
                WHERE DATE(hire_date) = ?;
                """;
        PreparedStatement ps = c.prepareStatement(DELETE_BY_DOCUMENT);
        ps.setDate(1, Date.valueOf(hireDateWithoutTime));
        return ps;
    }

    @Override
    public int deleteByDepartament(final Departament departament) {

        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = this.createQueryForDeleteByDepartament(c, departament.getId())) {

            int deletedRows = ps.executeUpdate();
            if (deletedRows == 0) {
                throw new DbConnectionException("No return, no employees have been sacked!");
            }

            return deletedRows;

        } catch (SQLException e) {
            throw new DbConnectionException(e.getMessage());
        }

    }

    private PreparedStatement createQueryForDeleteByDepartament(final Connection c,
                                                                final long departamentId)
            throws SQLException {

        final String DELETE_BY_DOCUMENT = """
                DELETE e FROM employees AS e
                INNER JOIN departaments_has_employees AS d
                ON e.id = d.id_employee
                WHERE d.id_departament = ?;
                """;
        PreparedStatement ps = c.prepareStatement(DELETE_BY_DOCUMENT);
        ps.setLong(1, departamentId);
        return ps;
    }

    //Return a specific type of Employee Base DTO with all atributtes
    private EmployeeBaseDTO createEmployeeBaseDTO(final Connection c,
                                                  final ResultSet rs0,
                                                  final long employeeId) {

        log.info("Tryning to find jobs and inheritance info.. \n");

        try (PreparedStatement ps1 = this.createQueryForFindJobsInformations(c, employeeId);
             ResultSet rs1 = ps1.executeQuery()) {

            //Base employee data
            final String name = rs0.getString("name");
            final LocalDate birthDate = rs0.getObject("birth_date", LocalDate.class);
            final int age = rs0.getInt("age");
            final String document = rs0.getString("document");
            final LocalDateTime hireDate = rs0.getObject("hire_date", LocalDateTime.class);

            final Map<Departament, Map<Level, BigDecimal>> dls = this.createJobsInformation(rs1);
            if (dls.isEmpty()) {
                throw new DbConnectionException("Employee without a job! Check your database!");
            }

            //Find normal, if exists return
            try (PreparedStatement ps2 = this.createQueryForFindNormalEmpById(c, employeeId);
                 ResultSet rs2 = ps2.executeQuery()) {
                if (rs2.next()) {
                    boolean hasFaculty = rs2.getBoolean("has_faculty");
                    return this.buildNormalEmployeeDTO(employeeId, name, birthDate, age, document, dls, hasFaculty, hireDate);
                }
            }

            //If not exists, find superior
            try (PreparedStatement ps3 = this.createQueryForFindSuperiorEmpById(c, employeeId);
                 ResultSet rs3 = ps3.executeQuery()) {
                rs3.next();
                int workExperience = rs3.getInt("work_experience");
                return this.buildSuperiorEmployeeDTO(employeeId, name, birthDate, age, document, dls, workExperience, hireDate);
            }

        } catch (SQLException e) {
            throw new DbConnectionException(e.getMessage());
        }
    }

    private PreparedStatement createQueryForFindJobsInformations(final Connection c, final long employeeId)
            throws SQLException {

        //Error
        final String FIND_JOBS_INFORMATIONS = """
                SELECT
                    d.id, d.name, d.creation_date, dhe.level, dhe.salary
                FROM
                    departaments AS d
                INNER JOIN
                    departaments_has_employees AS dhe
                ON
                    d.id = dhe.id_departament
                WHERE
                    dhe.id_employee = ?
                """;
        PreparedStatement ps = c.prepareStatement(FIND_JOBS_INFORMATIONS);
        ps.setLong(1, employeeId);
        return ps;
    }

    private Map<Departament, Map<Level, BigDecimal>> createJobsInformation(final ResultSet rs1)
            throws SQLException {

        //Ordered map
        final Map<Departament, Map<Level, BigDecimal>> dls = new TreeMap<>(
                Comparator.comparing(Departament::getName)
        );

        while (rs1.next()) {
            Long id = rs1.getLong("id");
            String name = rs1.getString("name");
            LocalDateTime creationDate = rs1.getObject("creation_date", LocalDateTime.class);
            Departament departament = Departament.builder()
                    .id(id)
                    .name(name)
                    .creationDate(creationDate)
                    .build();

            Level level = Level.valueOf(rs1.getString("level"));
            BigDecimal salary = rs1.getBigDecimal("salary");

            dls.put(departament, Map.of(level, salary));
        }

        return dls;
    }

    private PreparedStatement createQueryForFindNormalEmpById(final Connection c, final long employeeId)
            throws SQLException {

        final String FIND_NORMAL_EMP = """
                SELECT
                    ne.has_faculty
                FROM
                    normal_employees AS ne
                WHERE
                    ne.id = ?;
                """;

        PreparedStatement ps = c.prepareStatement(FIND_NORMAL_EMP);
        ps.setLong(1, employeeId);
        return ps;
    }

    private PreparedStatement createQueryForFindSuperiorEmpById(final Connection c, final long id)
            throws SQLException {

        final String FIND_NORMAL_EMP = """
                SELECT
                    se.work_experience
                FROM
                    superior_employees AS se
                INNER JOIN
                    employees AS e ON e.id = se.id
                WHERE
                    se.id = ?;
                """;

        PreparedStatement ps = c.prepareStatement(FIND_NORMAL_EMP);
        ps.setLong(1, id);
        return ps;
    }

    private EmployeeBaseDTO buildNormalEmployeeDTO(final long id,
                                                   final String name,
                                                   final LocalDate birthDate,
                                                   final int age,
                                                   final String document,
                                                   final Map<Departament, Map<Level, BigDecimal>> dls,
                                                   final boolean hasFaculty,
                                                   final LocalDateTime hireDate) {
        return NormalEmployeeDTO.builder()
                .id(id)
                .name(name)
                .birthDate(birthDate)
                .age(age)
                .document(document)
                .departamentsAndLevelsAndSalaries(dls)
                .hasFaculty(hasFaculty)
                .hireDate(hireDate)
                .build();
    }

    private EmployeeBaseDTO buildSuperiorEmployeeDTO(final long id,
                                                     final String name,
                                                     final LocalDate birthDate,
                                                     final int age,
                                                     final String document,
                                                     final Map<Departament, Map<Level, BigDecimal>> dls,
                                                     int workExperience,
                                                     final LocalDateTime hireDate) {
        return SuperiorEmployeeDTO.builder()
                .id(id)
                .name(name)
                .birthDate(birthDate)
                .age(age)
                .document(document)
                .departamentsAndLevelsAndSalaries(dls)
                .workExperience(workExperience)
                .hireDate(hireDate)
                .build();
    }


}
