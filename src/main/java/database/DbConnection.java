package database;

import exceptions.DbConnectionException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DbConnection {
    private static Connection connection;

    public static Connection getConnection() {

        try {
            if (connection == null || connection.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection("jdbc:mysql://localhost/company", "root", "root");
            }

        } catch (ClassNotFoundException e) {
            throw new DbConnectionException("MySQL Drive not found!");
        } catch (SQLException e) {
            throw new DbConnectionException(e.getMessage());
        }

        return connection;
    }

}
