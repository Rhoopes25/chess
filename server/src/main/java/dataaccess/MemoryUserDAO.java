package dataaccess;

import model.UserData;
import java.util.HashMap;

public class MemoryUserDAO implements UserDAO {
    // HashMap to store users: username -> UserData
    private HashMap<String, UserData> users = new HashMap<>();

    @Override
    public void createUser(UserData user) throws DataAccessException {
        //username is the key because it is unique
        users.put(user.username(), user);


    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        // returns user by username
        return users.get(username);
    }

    @Override
    public void clear() throws DataAccessException {
        //removes ALL users
        users.clear();
    }
}