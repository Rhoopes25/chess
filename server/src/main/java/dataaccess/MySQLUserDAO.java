package dataaccess;

import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;

public class MySQLUserDAO implements UserDAO {

    public MySQLUserDAO() {
        // Ensure DB and users table exist
        try {
            DatabaseManager.createDatabase();
        } catch (DataAccessException e) {
            throw new RuntimeException("Unable to create database", e);
        }

        try (var conn = DatabaseManager.getConnection()) {
            // NOTE: 'password' column stores the BCRYPT HASH
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
        // Hash the incoming plaintext password
        final String hashedPassword = BCrypt.hashpw(user.password(), BCrypt.gensalt());
        final String sql = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";

        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.username());
            ps.setString(2, hashedPassword); // store HASH, not plaintext
            ps.setString(3, user.email());
            ps.executeUpdate();

        } catch (SQLIntegrityConstraintViolationException dup) {
            // Make this message match your HTTP mapper (403 → "Error: already taken")
            throw new DataAccessException("Error: already taken");
        } catch (SQLException e) {
            throw new DataAccessException("Error creating user: " + e.getMessage());
        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        final String sql = "SELECT username, password, email FROM users WHERE username = ?";

        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            try (var rs = ps.executeQuery()) {
                if (!rs.next()) return null; // not found

                // 'password' here is the stored BCRYPT HASH
                String u = rs.getString("username");
                String hash = rs.getString("password");
                String email = rs.getString("email");
                return new UserData(u, hash, email);
            }

        } catch (SQLException e) {
            throw new DataAccessException("Error getting user: " + e.getMessage());
        }
    }

    @Override
    public void clear() throws DataAccessException {
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement("DELETE FROM users")) {
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error clearing users: " + e.getMessage());
        }
    }
}
