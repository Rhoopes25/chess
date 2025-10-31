package dataaccess;

import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;

public class MySQLUserDAO implements UserDAO {

    public MySQLUserDAO() {
        try {
            DatabaseManager.createDatabase();
        } catch (DataAccessException e) {
            throw new RuntimeException("Unable to create database", e);
        }

        try (var conn = DatabaseManager.getConnection()) {
            String createTable = """
            CREATE TABLE IF NOT EXISTS users (
                username VARCHAR(100) NOT NULL,
                password VARCHAR(100) NOT NULL,
                email VARCHAR(100) NOT NULL,
                PRIMARY KEY (username)
            )
            """;

            try (var stmt = conn.prepareStatement(createTable)) {
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Unable to create users table", e);
        } catch (DataAccessException e) {
            throw new RuntimeException("Unable to connect to database", e);
        }
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        // hash the password
        String hashedPassword = BCrypt.hashpw(user.password(), BCrypt.gensalt());

        try (var conn = DatabaseManager.getConnection()) {
            String sql = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";
            try (var stmt = conn.prepareStatement(sql)) {
                // Fill in the ? placeholders - they're numbered 1, 2, 3
                stmt.setString(1, user.username());      //  gets username
                stmt.setString(2, hashedPassword);        //  gets hashed password
                stmt.setString(3, user.email());          // gets email

                stmt.executeUpdate();  // Execute the INSERT
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error creating user");
        }

    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            String sql = "SELECT username, password, email FROM users WHERE username = ?";

            try (var stmt = conn.prepareStatement(sql)) {
                // Fill in the ? placeholder with the username parameter
                stmt.setString(1, username);

                // Execute the query and get results
                var rs = stmt.executeQuery();

                // Check if we found a user
                if (rs.next()) {
                    // Read the columns from the result
                    String foundUsername = rs.getString("username");
                    String foundPassword = rs.getString("password");
                    String foundEmail = rs.getString("email");

                    // Create and return a UserData object
                    return new UserData(foundUsername, foundPassword, foundEmail);
                } else {
                    // No user found with that username
                    return null;
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error getting user");
        }
    }

    @Override
    public void clear() throws DataAccessException {
        // Put your try-with-resources code HERE
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement("DELETE FROM users")) {
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error clearing users");        }
    }
}