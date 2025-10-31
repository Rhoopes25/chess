package dataaccess;

import model.UserData;
import org.mindrot.jbcrypt.BCrypt; // add bcrypt for hashing
import java.util.HashMap;

public class MemoryUserDAO implements UserDAO {
    // HashMap to store users: username -> UserData
    private HashMap<String, UserData> users = new HashMap<>();

    @Override
    public void createUser(UserData user) throws DataAccessException {
        //username is the key because it is unique
        // also: enforce uniqueness and store a BCRYPT HASH (not plaintext)
        if (users.containsKey(user.username())) {
            throw new DataAccessException("Error: already taken");
        }

        // hash the incoming plaintext password before storing
        String hashed = BCrypt.hashpw(user.password(), BCrypt.gensalt());

        // store a new UserData with the hashed password
        users.put(user.username(), new UserData(user.username(), hashed, user.email()));
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
