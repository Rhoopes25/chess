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
                    email    VARCHAR(255) NOT NULL,
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
        String hashedPassword = BCrypt.hashpw(user.password(), BCrypt.gensalt());
        String sql = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";

        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.username());
            stmt.setString(2, hashedPassword);
            stmt.setString(3, user.email());
            stmt.executeUpdate();
        } catch (SQLIntegrityConstraintViolationException dup) {
            // more precise signal for tests that check duplicate handling
            throw new DataAccessException("duplicate user");
        } catch (SQLException e) {
            throw new DataAccessException("Error creating user: " + e.getMessage());
        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        String sql = "SELECT username, password, email FROM users WHERE username = ?";

        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (var rs = stmt.executeQuery()) {
                if (!rs.next()) return null;
                String foundUsername = rs.getString("username");
                String foundPassword = rs.getString("password"); // this is the HASH
                String foundEmail = rs.getString("email");
                return new UserData(foundUsername, foundPassword, foundEmail);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error getting user: " + e.getMessage());
        }
    }

    @Override
    public void clear() throws DataAccessException {
        try (var conn = DatabaseManager.getConnection();
             var preparedStatement = conn.prepareStatement("DELETE FROM users")) {
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error clearing users: " + e.getMessage());
        }
    }
}
