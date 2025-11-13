package client;

import org.junit.jupiter.api.*;
import server.Server;

public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade(port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    public void setup() throws Exception {
        // Clear the database before each test
        facade.clear();
    }

    @Test
    public void registerSuccess() throws Exception {
        // Register a new user
        var result = facade.register("testuser", "password123", "test@email.com");

        // Check that we got an authToken back
        Assertions.assertNotNull(result.authToken());
        Assertions.assertTrue(result.authToken().length() > 10);

        // Check that username matches
        Assertions.assertEquals("testuser", result.username());
    }
    @Test
    public void registerDuplicate() throws Exception {
        // Register a user first time - should succeed
        facade.register("testuser", "password123", "test@email.com");

        // Try to register same username again - should fail
        Exception exception = Assertions.assertThrows(Exception.class, () -> {
            facade.register("testuser", "differentpass", "different@email.com");
        });

        // Check that it failed (threw an exception)
        Assertions.assertTrue(exception.getMessage().contains("403") ||
                exception.getMessage().contains("Register failed"));
    }
    @Test
    public void loginSuccess() throws Exception {
        // First register a user
        facade.register("testuser", "password123", "test@email.com");

        // Now try to login with correct credentials
        var result = facade.login("testuser", "password123");

        // Check that we got an authToken back
        Assertions.assertNotNull(result.authToken());
        Assertions.assertTrue(result.authToken().length() > 10);

        // Check that username matches
        Assertions.assertEquals("testuser", result.username());
    }

    @Test
    public void loginFailure() throws Exception {
        // Register a user
        facade.register("testuser", "password123", "test@email.com");

        // Try to login with WRONG password - should fail
        Exception exception = Assertions.assertThrows(Exception.class, () -> {
            facade.login("testuser", "wrongpassword");
        });

        // Check that it failed (401 = unauthorized)
        Assertions.assertTrue(exception.getMessage().contains("401") ||
                exception.getMessage().contains("Login failed"));
    }
    @Test
    public void logoutSuccess() throws Exception {
        // Register and get authToken
        var registerResult = facade.register("testuser", "password123", "test@email.com");

        // Logout should not throw an exception
        Assertions.assertDoesNotThrow(() -> {
            facade.logout(registerResult.authToken());
        });
    }

    @Test
    public void logoutFailure() throws Exception {
        // Try to logout with invalid/fake authToken - should fail
        Exception exception = Assertions.assertThrows(Exception.class, () -> {
            facade.logout("invalidAuthToken12345");
        });

        // Check that it failed (401 = unauthorized)
        Assertions.assertTrue(exception.getMessage().contains("401") ||
                exception.getMessage().contains("Logout failed"));
    }

    @Test
    public void createGameSuccess() throws Exception {
        // Register and get authToken
        var registerResult = facade.register("testuser", "password123", "test@email.com");

        // Create a game
        var result = facade.createGame(registerResult.authToken(), "MyChessGame");

        // Check that we got a gameID back
        Assertions.assertTrue(result.gameID() > 0);
    }

    @Test
    public void createGameFailure() throws Exception {
        // Try to create game without being logged in (invalid authToken)
        Exception exception = Assertions.assertThrows(Exception.class, () -> {
            facade.createGame("invalidAuthToken", "MyGame");
        });

        // Check that it failed (401 = unauthorized)
        Assertions.assertTrue(exception.getMessage().contains("401") ||
                exception.getMessage().contains("Create game failed"));
    }
    @Test
    public void listGamesSuccess() throws Exception {
        // Register and get authToken
        var registerResult = facade.register("testuser", "password123", "test@email.com");

        // Create a couple of games
        facade.createGame(registerResult.authToken(), "Game1");
        facade.createGame(registerResult.authToken(), "Game2");

        // List games
        var result = facade.listGames(registerResult.authToken());

        // Check that we got 2 games back
        Assertions.assertNotNull(result.games());
        Assertions.assertEquals(2, result.games().length);
    }

    @Test
    public void listGamesFailure() throws Exception {
        // Try to list games without being logged in (invalid authToken)
        Exception exception = Assertions.assertThrows(Exception.class, () -> {
            facade.listGames("invalidAuthToken");
        });

        // Check that it failed (401 = unauthorized)
        Assertions.assertTrue(exception.getMessage().contains("401") ||
                exception.getMessage().contains("List games failed"));
    }

    @Test
    public void joinGameSuccess() throws Exception {
        // Register and get authToken
        var registerResult = facade.register("testuser", "password123", "test@email.com");

        // Create a game
        var gameResult = facade.createGame(registerResult.authToken(), "MyGame");

        // Join the game as WHITE - should not throw exception
        Assertions.assertDoesNotThrow(() -> {
            facade.joinGame(registerResult.authToken(), "WHITE", gameResult.gameID());
        });
    }

    @Test
    public void joinGameFailure() throws Exception {
        // Register and create a game
        var registerResult = facade.register("testuser", "password123", "test@email.com");
        var gameResult = facade.createGame(registerResult.authToken(), "MyGame");

        // Join as WHITE first
        facade.joinGame(registerResult.authToken(), "WHITE", gameResult.gameID());

        // Try to join as WHITE again. Should fail (already taken)
        Exception exception = Assertions.assertThrows(Exception.class, () -> {
            facade.joinGame(registerResult.authToken(), "WHITE", gameResult.gameID());
        });

        // Check that it failed (403 = already taken)
        Assertions.assertTrue(exception.getMessage().contains("403") ||
                exception.getMessage().contains("Join game failed"));
    }


}