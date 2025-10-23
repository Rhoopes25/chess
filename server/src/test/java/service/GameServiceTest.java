package service;

import dataaccess.*;
import model.*;
import chess.ChessGame;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class GameServiceTest {

    private GameService gameService;
    private GameDAO gameDAO;
    private AuthDAO authDAO;

    @BeforeEach
    public void setUp() {
        // fresh in-memory DAOs for each test
        authDAO = new MemoryAuthDAO();
        gameDAO = new MemoryGameDAO();

        // Initialize the service with these test DAOs
        gameService = new GameService(authDAO, gameDAO);
    }

    @Test
    @DisplayName("Create Game Successfully")
    public void createGameSuccess() throws DataAccessException {
        // Create a valid authToken
        AuthData validAuth = new AuthData("auth1", "User1");

        authDAO.createAuth(validAuth);

        //  game request with name
        CreateGameRequest request = new CreateGameRequest("Game1");

        // Create the game
        CreateGameResponse response = gameService.createGame(request, "auth1");

        // Verify it worked
        assertNotNull(response, "Response should not be null");
        assertTrue(response.gameID() > 0, "GameID should be positive");

        // Verify game was stored in database
        GameData storedGame = gameDAO.getGame(response.gameID());

        assertNotNull(storedGame, "Game should be stored in database");
        assertEquals("Game1", storedGame.gameName(), "Game name should match");
    }

    @Test
    @DisplayName("Create Game Fails With Invalid AuthToken")
    public void createGameFailsInvalidAuth() throws DataAccessException {
        // Use a fake authToken that doesn't exist
        String fakeToken = "fakeToken";
        CreateGameRequest request = new CreateGameRequest("TestGame");

        // 2. Should throw exception
        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            gameService.createGame(request, fakeToken);
        });

        // Check error message
        assertEquals("Error: unauthorized", exception.getMessage());
    }

    @Test
    @DisplayName("List Games Successfully")
    public void listGamesSuccess() throws DataAccessException {
        // Create a valid authToken
        AuthData validAuth = new AuthData("auth2", "User2");

        authDAO.createAuth(validAuth);

        // Create a few games to list
        GameData game1 = new GameData(1, null, null, "Game1", new ChessGame());
        GameData game2 = new GameData(2, null, null, "Game2", new ChessGame());

        gameDAO.createGame(game1);
        gameDAO.createGame(game2);

        // List the games
        ListGamesResponse response = gameService.listGames("auth2");

        // Verify it worked
        assertNotNull(response, "Response should not be null");
        assertNotNull(response.games(), "Games collection should not be null");

        assertEquals(2, response.games().size(), "Should have 2 games");
    }

    @Test
    @DisplayName("List Games Fails With Invalid AuthToken")
    public void listGamesFailsInvalidAuth() throws DataAccessException {
        // Use a fake authToken
        String fakeToken = "FAKETOKEN";

        // Should throw exception
        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            gameService.listGames(fakeToken);
        });

        // 3. ASSERT - Check error message
        assertEquals("Error: unauthorized", exception.getMessage());
    }

    @Test
    @DisplayName("Join Game Successfully As White Player")
    public void joinGameSuccess() throws DataAccessException {
        // Create valid authToken and a game
        AuthData validAuth = new AuthData("auth1", "User1");
        authDAO.createAuth(validAuth);

        // Create a game to join
        GameData game = new GameData(1, null, null, "Game1", new ChessGame());
        gameDAO.createGame(game);

        // Create join request
        JoinGameRequest request = new JoinGameRequest("WHITE", 1);

        // Join the game
        gameService.joinGame(request, "auth1");

        // Verify the player was added
        GameData updatedGame = gameDAO.getGame(1);

        assertNotNull(updatedGame, "Game should exist");
        assertEquals("User1", updatedGame.whiteUsername(), "White player should be set");
    }

    @Test
    @DisplayName("Join Game Fails When Spot Already Taken")
    public void joinGameFailsSpotTaken() throws DataAccessException {
        // Create two auth tokens for two different players
        AuthData auth1 = new AuthData("token1", "Player1");
        AuthData auth2 = new AuthData("token2", "Player2");
        authDAO.createAuth(auth1);
        authDAO.createAuth(auth2);

        // Create a game
        GameData game = new GameData(1, null, null, "TestGame", new ChessGame());
        gameDAO.createGame(game);

        // Player1 joins as WHITE first
        JoinGameRequest firstJoin = new JoinGameRequest("WHITE", 1);

        gameService.joinGame(firstJoin, "token1");

        // Player2 tries to join as WHITE too
        JoinGameRequest secondJoin = new JoinGameRequest("WHITE", 1);

        // Should throw exception
        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            gameService.joinGame(secondJoin, "token2");
        });

        // Check error message
        assertEquals("Error: already taken", exception.getMessage());
    }

}