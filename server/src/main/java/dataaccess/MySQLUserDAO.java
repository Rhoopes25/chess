package dataaccess;

import model.UserData;
import java.sql.*;

public class MySQLUserDAO implements UserDAO {

    @Override
    public void createUser(UserData user) throws DataAccessException {

    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        return null;
    }

    @Override
    public void clear() throws DataAccessException {
        // Put your try-with-resources code HERE
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement("DELETE FROM users")) {
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error clearing users: " + e.getMessage());        }
    }
}