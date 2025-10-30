package dataaccess;

import model.AuthData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;

public class MySQLAuthDAO implements AuthDAO {
    @Override
    public void createAuth(AuthData auth) throws DataAccessException {

        try (var conn = DatabaseManager.getConnection()) {
            String sql = "INSERT INTO auth (authToken, username) VALUES (?, ?)";
            try (var stmt = conn.prepareStatement(sql)) {
                // Fill in the ? placeholders - they're numbered 1, 2, 3
                stmt.setString(1, auth.authToken());   //  gets authToken
                stmt.setString(2, auth.username());    //  gets username

                stmt.executeUpdate();  // Execute the INSERT
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error creating auth: " + e.getMessage());
        }

    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            String sql = "SELECT authToken, username FROM auth WHERE authToken = ?";

            try (var stmt = conn.prepareStatement(sql)) {
                // Fill in the ? placeholder with the username parameter
                stmt.setString(1, authToken);

                // Execute the query and get results
                var rs = stmt.executeQuery();

                // Check if we found a authToken
                if (rs.next()) {
                    // Read the columns from the result
                    String foundAuthToken = rs.getString("authToken");
                    String foundUsername = rs.getString("username");

                    // Create and return a UserData object
                    return new AuthData(foundAuthToken, foundUsername);
                } else {
                    // No user found with that username
                    return null;
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error getting auth: " + e.getMessage());
        }
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            String sql = "DELETE FROM auth WHERE authToken = ?";
            try (var stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, authToken);  // Fill in the ? with the authToken
                stmt.executeUpdate();           // Execute the DELETE
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error deleting auth: " + e.getMessage());
        }
    }

    @Override
    public void clear() throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement("DELETE FROM auth")) {
                preparedStatement.executeUpdate();
            }

        } catch (SQLException e) {
            throw new DataAccessException("Error clearing auth: " + e.getMessage());
        }

    }

}