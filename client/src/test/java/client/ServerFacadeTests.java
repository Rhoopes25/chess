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

}