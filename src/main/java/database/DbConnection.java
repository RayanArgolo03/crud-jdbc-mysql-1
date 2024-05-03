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
                connection = DriverManager.getConnection("jdbc:mysql://localhost/company", loadProperties());
            }

        } catch (ClassNotFoundException e) {
            throw new DbConnectionException("MySQL Drive not found!");
        } catch (SQLException e) {
            throw new DbConnectionException(e.getMessage());
        }

        return connection;
    }

    private static Properties loadProperties() {

        try (FileInputStream inputStream = new FileInputStream("db.properties")) {

            final Properties properties = new Properties();
            properties.load(inputStream);
            return properties;

        } catch (IOException e) {
            throw new DbConnectionException("Error! " + e.getMessage());
        }

    }
}
