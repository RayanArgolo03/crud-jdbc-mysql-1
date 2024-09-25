package repositories.impl;

import org.bson.types.ObjectId;
import repositories.interfaces.UserRepository;
import database.DbConnection;
import model.user.User;
import dtos.UserResponse;
import exceptions.DbConnectionException;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.sql.*;
import java.util.Optional;

@Log4j2
@NoArgsConstructor
public final class UserRepositoryImpl implements UserRepository {

    @Override
    public void save(final User user) {

        log.info("Tryning to save {} in the database.. \n", user.getUsername());

        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = this.createQueryForSaveUser(c, user);
             ResultSet rs = this.executeSaveUser(ps)) {

            rs.next();
            final Long id = rs.getLong(1);

        } catch (SQLException e) {
            throw new DbConnectionException(e.getMessage());
        }
    }


    private PreparedStatement createQueryForSaveUser(final Connection c, final User user)
            throws SQLException {

        final String SAVE_USER = """
                INSERT INTO users (username, password) VALUES (?, ?)
                """;

        PreparedStatement ps = c.prepareStatement(SAVE_USER, Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, user.getUsername());
        ps.setString(2, user.getPassword());
        return ps;
    }

    private ResultSet executeSaveUser(final PreparedStatement ps)
            throws SQLException {
        ps.execute();
        return ps.getGeneratedKeys();
    }

    //Return user id for print
    @Override
    public int deleteById(ObjectId id) {

        log.info("Tryning to delete user with id {}.. \n", id);

        try (Connection c = DbConnection.getConnection()){
//             PreparedStatement ps = this.createQueryForDeleteUser(c, id)) {

//            if (ps.executeUpdate() == 0) throw new DbConnectionException("Exclusion not completed!");
            if (c.createStatement().executeUpdate("") == 0) throw new DbConnectionException("Exclusion not completed!");

            //Workaround hehehe
            return Integer.parseInt(String.valueOf(id));

        } catch (SQLException e) {
            throw new DbConnectionException(e.getMessage());
        }

    }

    private PreparedStatement createQueryForDeleteUser(final Connection c, final Long id)
            throws SQLException {

        PreparedStatement ps = c.prepareStatement("DELETE FROM users AS u WHERE u.id = ?");
        ps.setLong(1, id);
        return ps;
    }

    @Override
    public Optional<String> findUsername(final String username) {

        log.info("Tryning to find user with username {} in the database.. \n", username);

        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = this.createQueryForFindUsername(c, username);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) return Optional.of(rs.getString("username"));

        } catch (SQLException e) {
            throw new DbConnectionException(e.getMessage());
        }

        return Optional.empty();
    }

    private PreparedStatement createQueryForFindUsername(final Connection c, final String username)
            throws SQLException {

        final String FIND_USERNAME = """
                SELECT u.username FROM users AS u WHERE u.username = BINARY ?
                """;
        PreparedStatement ps = c.prepareStatement(FIND_USERNAME);
        ps.setString(1, username);
        return ps;
    }

    @Override
    public Optional<User> findUser(final String username, final String password) {

        log.info("Tryning to find {} in the database.. \n", username);

        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = this.createQueryForFindUser(c, username, password);
             ResultSet rs = ps.executeQuery()) {

//            if (rs.next()) return Optional.of(this.createUserDTO(rs));
            return null;
        } catch (SQLException e) {
            throw new DbConnectionException(e.getMessage());
        }

//        return Optional.empty();
    }

    private PreparedStatement createQueryForFindUser(final Connection c, final String username,
                                                     final String password)
            throws SQLException {

        final String FIND_USER = """
                SELECT * FROM users AS u WHERE u.username = BINARY ? AND u.password = BINARY ?
                """;

        PreparedStatement ps = c.prepareStatement(FIND_USER);
        ps.setString(1, username);
        ps.setString(2, password);
        return ps;
    }


    private UserResponse createUserDTO(final ResultSet rs) throws SQLException {
        final Long id = rs.getLong("id");
        final String username = rs.getString("username");
        final String password = rs.getString("password");
//        return new UserResponse(id, username, password);
        return null;
    }


}
