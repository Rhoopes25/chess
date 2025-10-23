package service;

import dataaccess.*;
import model.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class ClearServiceTest {

    private ClearService clearService;
    private UserDAO userDAO;
    private AuthDAO authDAO;
    private GameDAO gameDAO;

    @BeforeEach
    public void setUp() {
        // Create fresh in-memory DAOs for each test
        userDAO = new MemoryUserDAO();
        authDAO = new MemoryAuthDAO();
        gameDAO = new MemoryGameDAO();

        // Initialize the service with these test DAOs
        clearService = new ClearService(userDAO, authDAO, gameDAO);
    }

    @Test
    @DisplayName("Clear Service Wipes All Data")
    public void clearSuccess() throws DataAccessException {
        // 1. Arrange: Add data to all databases
        userDAO.createUser(new UserData("user1", "pass1", "email1"));
        authDAO.createAuth(new AuthData("token1", "user1"));

        // (Assuming you have a GameData constructor and a createGame method)
        // We'll create a game with ID 1
        GameData testGame = new GameData(1, null, null, "testGame", null);
        gameDAO.createGame(testGame); // <-- Add a game to be cleared


        // 2. Act: Run the service method
        clearService.clear();


        // 3. Assert: Verify all data is gone
        assertNull(userDAO.getUser("user1"), "User should be cleared");
        assertNull(authDAO.getAuth("token1"), "Auth token should be cleared");
        assertNull(gameDAO.getGame(1), "Game should be cleared"); // <-- Verify game is gone
    }
}