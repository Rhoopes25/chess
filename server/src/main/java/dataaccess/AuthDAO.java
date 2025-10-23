package dataaccess;
import model.AuthData;

public interface AuthDAO {
    //create auth token
    void createAuth(AuthData auth) throws DataAccessException;

    //get auth token
    AuthData getAuth(String authToken) throws DataAccessException;

    //delete auth token
    void deleteAuth(String authToken) throws DataAccessException;

    //clear all auth data
    void clear() throws DataAccessException;
}
