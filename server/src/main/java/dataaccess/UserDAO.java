package dataaccess;
import model.UserData;

public interface UserDAO {
    // Create or add a user
    void createUser(UserData user) throws DataAccessException;

    // Get a user by username
    UserData getUser(String username) throws DataAccessException;

    // Clear all users
    void clear() throws DataAccessException;
}
