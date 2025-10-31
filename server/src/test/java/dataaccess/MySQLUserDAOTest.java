package dataaccess;

import model.UserData;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class MySQLUserDAOTest {
    private MySQLUserDAO userDAO;
    private MySQLAuthDAO authDAO;

    @BeforeEach
    public void setup() throws DataAccessException {
        userDAO = new MySQLUserDAO();
        authDAO = new MySQLAuthDAO();

        // clear auth first because of foreign key
        authDAO.clear();
        userDAO.clear();
    }

    @Test
    public void createUserPositive() throws DataAccessException {
        UserData user = new UserData("testuser", "password123", "test@email.com");
        userDAO.createUser(user);

        UserData result = userDAO.getUser("testuser");
        assertNotNull(result);
        assertEquals("testuser", result.username());
    }

    @Test
    public void createUserNegative() throws DataAccessException {
        UserData user = new UserData("testuser", "password123", "test@email.com");
        userDAO.createUser(user);

        // try to create same user again - should throw exception
        assertThrows(DataAccessException.class, () -> userDAO.createUser(user));
    }

    @Test
    public void getUserPositive() throws DataAccessException {
        UserData user = new UserData("john", "pass", "john@test.com");
        userDAO.createUser(user);

        UserData result = userDAO.getUser("john");
        assertNotNull(result);
        assertEquals("john", result.username());
        assertEquals("john@test.com", result.email());
    }

    @Test
    public void getUserNegative() throws DataAccessException {
        UserData result = userDAO.getUser("doesntexist");
        assertNull(result);
    }

    @Test
    public void clearPositive() throws DataAccessException {
        userDAO.createUser(new UserData("user1", "pass1", "user1@test.com"));
        userDAO.createUser(new UserData("user2", "pass2", "user2@test.com"));

        authDAO.clear();
        userDAO.clear();

        assertNull(userDAO.getUser("user1"));
        assertNull(userDAO.getUser("user2"));
    }
}