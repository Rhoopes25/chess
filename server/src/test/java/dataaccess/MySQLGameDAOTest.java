package dataaccess;

import chess.ChessGame;
import model.GameData;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Collection;

public class MySQLGameDAOTest {
    private MySQLGameDAO gameDAO;

    @BeforeEach
    public void setup() throws DataAccessException {
        gameDAO = new MySQLGameDAO();
        gameDAO.clear();
    }

    @Test
    public void createGamePositive() throws DataAccessException {
        ChessGame game = new ChessGame();
        GameData gameData = new GameData(0, null, null, "testgame", game);

        int gameID = gameDAO.createGame(gameData);

        assertTrue(gameID > 0);
        GameData result = gameDAO.getGame(gameID);
        assertNotNull(result);
        assertEquals("testgame", result.gameName());
    }

    @Test
    public void createGameNegative() throws DataAccessException {
        // game with null gameName should fail
        GameData gameData = new GameData(0, null, null, null, new ChessGame());
        assertThrows(DataAccessException.class, () -> gameDAO.createGame(gameData));
    }

    @Test
    public void getGamePositive() throws DataAccessException {
        ChessGame game = new ChessGame();
        GameData gameData = new GameData(0, "white", "black", "mygame", game);
        int id = gameDAO.createGame(gameData);

        GameData result = gameDAO.getGame(id);
        assertNotNull(result);
        assertEquals("mygame", result.gameName());
        assertEquals("white", result.whiteUsername());
        assertEquals("black", result.blackUsername());
    }

    @Test
    public void getGameNegative() throws DataAccessException {
        GameData result = gameDAO.getGame(9999);
        assertNull(result);
    }

    @Test
    public void listGamesPositive() throws DataAccessException {
        gameDAO.createGame(new GameData(0, null, null, "game1", new ChessGame()));
        gameDAO.createGame(new GameData(0, null, null, "game2", new ChessGame()));
        gameDAO.createGame(new GameData(0, null, null, "game3", new ChessGame()));

        Collection<GameData> games = gameDAO.listGames();
        assertEquals(3, games.size());
    }

    @Test
    public void listGamesNegative() throws DataAccessException {
        // empty list should still work
        Collection<GameData> games = gameDAO.listGames();
        assertNotNull(games);
        assertEquals(0, games.size());
    }

    @Test
    public void updateGamePositive() throws DataAccessException {
        ChessGame game = new ChessGame();
        GameData gameData = new GameData(0, null, null, "game", game);
        int id = gameDAO.createGame(gameData);

        GameData updated = new GameData(id, "whitePlayer", "blackPlayer", "game", game);
        gameDAO.updateGame(updated);

        GameData result = gameDAO.getGame(id);
        assertEquals("whitePlayer", result.whiteUsername());
        assertEquals("blackPlayer", result.blackUsername());
    }

    @Test
    public void updateGameNegative() throws DataAccessException {
        // updating game that doesn't exist - should still run but not update anything
        ChessGame game = new ChessGame();
        GameData fake = new GameData(9999, "white", "black", "fake", game);

        // this shouldn't throw error, just won't update anything
        gameDAO.updateGame(fake);

        GameData result = gameDAO.getGame(9999);
        assertNull(result);
    }

    @Test
    public void clearPositive() throws DataAccessException {
        gameDAO.createGame(new GameData(0, null, null, "game1", new ChessGame()));
        gameDAO.createGame(new GameData(0, null, null, "game2", new ChessGame()));

        gameDAO.clear();

        Collection<GameData> games = gameDAO.listGames();
        assertEquals(0, games.size());
    }
}