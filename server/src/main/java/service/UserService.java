package service;

import model.UserData;
import model.AuthData;
import java.util.UUID;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.UserDAO;
import model.*;

public class UserService {
    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public UserService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    public RegisterResponse register(RegisterRequest request) throws DataAccessException {
        if (request.username() == null || request.password() == null || request.email() == null) {
            throw new DataAccessException("Error: bad request");
        }
        if(userDAO.getUser(request.username()) != null) {
            throw new DataAccessException("Error: already taken");
        }

        // create user
        UserData newUser = new UserData(request.username(), request.password(), request.email());
        userDAO.createUser(newUser);

        // Generate a new authToken
        String authToken = UUID.randomUUID().toString();

        // auth token for user
        AuthData authData = new AuthData(authToken, request.username());
        authDAO.createAuth(authData);

        // return username and token
        return new RegisterResponse(request.username(), authToken);

    }

    public LoginResponse login(LoginRequest request) throws DataAccessException {
        // validate - username and password provided?
        if (request.username() == null || request.password() == null) {
            throw new DataAccessException("Error: bad request");
        }
        // get user from UserDAO
        UserData user = userDAO.getUser(request.username());

        // does user exist and does password match?
        if (user == null || !user.password().equals(request.password())) {
            throw new DataAccessException("Error: unauthorized");
        }
        String authToken = UUID.randomUUID().toString();

        // auth token for user
        AuthData authData = new AuthData(authToken, request.username());
        authDAO.createAuth(authData);

        // return username and token
        return new LoginResponse(request.username(), authToken);
    }

    public void logout(String authToken) throws DataAccessException {
        // Check if authToken exists
        if (authDAO.getAuth(authToken) == null) {
            throw new DataAccessException("Error: unauthorized");
        }
        //removes the authToken from storage, so it's no longer valid.
        authDAO.deleteAuth(authToken);
    }

}
