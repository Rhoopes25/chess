package dataaccess;

import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class MySQLAuthDAOTest {
    private MySQLAuthDAO authDAO;
    private MySQLUserDAO userDAO;

    @BeforeEach
    public void setup() throws DataAccessException {
        authDAO = new MySQLAuthDAO();
        userDAO = new MySQLUserDAO();

        // clear in correct order
        authDAO.clear();
        userDAO.clear();

        // create a test user for auth tests (needed for foreign key)
        userDAO.createUser(new UserData("testuser", "password", "test@email.com"));
    }

    @Test
    public void createAuthPositive() throws DataAccessException {
        AuthData auth = new AuthData("token123", "testuser");
        authDAO.createAuth(auth);

        AuthData result = authDAO.getAuth("token123");
        assertNotNull(result);
        assertEquals("token123", result.authToken());
        assertEquals("testuser", result.username());
    }

    @Test
    public void createAuthNegative() throws DataAccessException {
        AuthData auth = new AuthData("token123", "testuser");
        authDAO.createAuth(auth);

        // try same token again - should throw exception
        AuthData auth2 = new AuthData("token123", "testuser");
        assertThrows(DataAccessException.class, () -> authDAO.createAuth(auth2));
    }

    @Test
    public void getAuthPositive() throws DataAccessException {
        AuthData auth = new AuthData("mytoken", "testuser");
        authDAO.createAuth(auth);

        AuthData result = authDAO.getAuth("mytoken");
        assertNotNull(result);
        assertEquals("testuser", result.username());
        assertEquals("mytoken", result.authToken());
    }

    @Test
    public void getAuthNegative() throws DataAccessException {
        AuthData result = authDAO.getAuth("badtoken");
        assertNull(result);
    }

    @Test
    public void deleteAuthPositive() throws DataAccessException {
        AuthData auth = new AuthData("token456", "testuser");
        authDAO.createAuth(auth);

        authDAO.deleteAuth("token456");

        AuthData result = authDAO.getAuth("token456");
        assertNull(result);
    }

    @Test
    public void deleteAuthNegative() throws DataAccessException {
        // deleting nonexistent token should not throw error
        authDAO.deleteAuth("notreal");

        // verify it didn't break anything
        AuthData result = authDAO.getAuth("notreal");
        assertNull(result);
    }

    @Test
    public void clearPositive() throws DataAccessException {
        authDAO.createAuth(new AuthData("token1", "testuser"));
        authDAO.createAuth(new AuthData("token2", "testuser"));

        authDAO.clear();

        assertNull(authDAO.getAuth("token1"));
        assertNull(authDAO.getAuth("token2"));
    }
}