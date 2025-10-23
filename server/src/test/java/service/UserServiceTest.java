package service;

import dataaccess.*;
import model.*;
import org.junit.jupiter.api.*;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {

    private UserService userService;
    private UserDAO userDAO;
    private AuthDAO authDAO;

    @BeforeEach
    public void setUp() {
        // Create fresh in-memory DAOs for each test
        userDAO = new MemoryUserDAO();
        authDAO = new MemoryAuthDAO();

        // Initialize the service with these test DAOs
        userService = new UserService(userDAO, authDAO);
    }

    @Test
    @DisplayName("Register New User Successfully")
    public void registerSuccess() throws DataAccessException {
        // Create a valid register request
        RegisterRequest request = new RegisterRequest("newUser", "password123", "user@email.com");

        // Call the register method
        RegisterResponse response = userService.register(request);

        // did it work?
        assertNotNull(response, "Response should not be null");
        assertEquals("newUser", response.username(), "Username should match");
        assertNotNull(response.authToken(), "AuthToken should be generated");

        // Verify user was actually stored in the database
        UserData storedUser = userDAO.getUser("newUser");
        assertNotNull(storedUser, "User should be stored in database");
        assertEquals("newUser", storedUser.username());

        // Verify authToken was stored in the database
        AuthData storedAuth = authDAO.getAuth(response.authToken());
        assertNotNull(storedAuth, "Auth token should be stored in database");
        assertEquals("newUser", storedAuth.username(), "Auth should be linked to correct user");
    }

    @Test
    @DisplayName("Register Fails When Username Already Taken")
    public void registerFailsUsernameTaken() throws DataAccessException {
        // Register a user first
        RegisterRequest firstRequest = new RegisterRequest("existingUser", "pass123", "email@test.com");
        userService.register(firstRequest);

        //  register with the same username but different password/email
        RegisterRequest duplicateRequest = new RegisterRequest("existingUser", "password", "example@com");

        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            userService.register(duplicateRequest);
        });
        // just like in UserService register error
        assertEquals("Error: already taken", exception.getMessage());
    }

    @Test
    @DisplayName("Login Existing User Successfully")
    public void loginSuccess() throws DataAccessException {
        //  Register a user so they exist in the database
        RegisterRequest registerRequest = new RegisterRequest("User25", "password25", "email@25");
        userService.register(registerRequest);

        // Login request with the SAME credentials
        LoginRequest loginRequest = new LoginRequest("User25", "password25");

        // Call login method
        LoginResponse response = userService.login(loginRequest);

        // Verify it worked
        assertNotNull(response, "Response should not be null");
        assertEquals("User25", response.username(), "Username should match");

        assertNotNull(response.authToken(), "AuthToken should be generated");

        // Verify the NEW authToken was stored in database
        AuthData storedAuth = authDAO.getAuth(response.authToken());

        assertNotNull(storedAuth, "New auth token should be stored");
        assertEquals("User25", storedAuth.username(), "Auth should be linked to correct user");
    }

    @Test
    @DisplayName("Login Fails With Wrong Password")
    public void loginFailsWrongPassword() throws DataAccessException {
        // Register a user first
        RegisterRequest registerRequest = new RegisterRequest("User24", "Password24", "email@24.com");
        userService.register(registerRequest);

        // Try to log in with WRONG password
        LoginRequest loginRequest = new LoginRequest("User24", "iLoveCatsMan");

        //  throw exception
        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            userService.login(loginRequest);
        });

        // Check error message
        assertEquals("Error: unauthorized", exception.getMessage());
    }

    @Test
    @DisplayName("Logout Successfully Removes AuthToken")
    public void logoutSuccess() throws DataAccessException {
        // Register and login to get an authToken
        RegisterRequest registerRequest = new RegisterRequest("User20", "password20", "email@20");
        userService.register(registerRequest);

        LoginRequest loginRequest = new LoginRequest("User20", "password20");

        // login method
        LoginResponse response = userService.login(loginRequest);

         var token = response.authToken();

        // logout with  authToken
        userService.logout(token);

        // 3. ASSERT - Verify the authToken was deleted from database
        AuthData deletedAuth = authDAO.getAuth(token);
        assertNull(deletedAuth, "AuthToken should be deleted from database");

    }

    @Test
    @DisplayName("Logout Fails With Invalid AuthToken")
    public void logoutFailsInvalidToken() throws DataAccessException {
        // Use a fake authToken that doesn't exist in database
        var fakeAuthToken = UUID.randomUUID().toString();

        // Should throw exception
        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            userService.logout(fakeAuthToken);
        });
        // 3. ASSERT - Check error message
        assertEquals("Error: unauthorized", exception.getMessage());
    }

}