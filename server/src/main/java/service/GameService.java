package service;

import chess.ChessGame;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.*;

import java.util.Collection;

public class GameService {
    private final AuthDAO authDAO;
    private final GameDAO gameDAO;

    public GameService(AuthDAO authDAO, GameDAO gameDAO) {
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;

    }
    public CreateGameResponse createGame(CreateGameRequest request, String authToken) throws DataAccessException {
        //  authToken is not valid
        if (authDAO.getAuth(authToken) == null) {
            throw new DataAccessException("Error: unauthorized");
        }

        // gameName is not provided
        // you need name
        if (request.gameName() == null) {
            throw new DataAccessException("Error: bad request");
        }

        // new chess game and GameData
        ChessGame newChessGame = new ChessGame();
        GameData gameData = new GameData(0, null, null, request.gameName(), newChessGame);

        // Store the game and get the generated gameID
        int gameID = gameDAO.createGame(gameData);

        // Return response with gameID
        return new CreateGameResponse(gameID);
    }

    public ListGamesResponse listGames(String authToken) throws DataAccessException {
        //  authToken is not valid
        if (authDAO.getAuth(authToken) == null) {
            throw new DataAccessException("Error: unauthorized");
        }

        // Get all games
        Collection<GameData> games = gameDAO.listGames();

        // Return list of games
        return new ListGamesResponse(games);
    }

    public void joinGame(JoinGameRequest request, String authToken) throws DataAccessException {
        // Verify authToken is valid and get the username
        AuthData auth = authDAO.getAuth(authToken);
        if (auth == null) {
            throw new DataAccessException("Error: unauthorized");
        }
        String username = auth.username();

        // gameID not provided
        if (request.gameID() == 0) {
            throw new DataAccessException("Error: bad request");
        }

        // Get game
        GameData game = gameDAO.getGame(request.gameID());
        if (game == null) {
            throw new DataAccessException("Error: bad request");
        }

        // Handle joining as a player (WHITE or BLACK)
        if (request.playerColor() != null) {
            // Validate color is WHITE or BLACK
            if (!request.playerColor().equals("WHITE") && !request.playerColor().equals("BLACK")) {
                throw new DataAccessException("Error: bad request");
            }

            // white spot is already taken
            if (request.playerColor().equals("WHITE")) {
                if (game.whiteUsername() != null) {
                    throw new DataAccessException("Error: already taken");
                }
                // Update game with white player
                GameData updatedGame = new GameData(game.gameID(), username, game.blackUsername(),
                        game.gameName(), game.game());
                gameDAO.updateGame(updatedGame);
            } else { // BLACK
                if (game.blackUsername() != null) {
                    throw new DataAccessException("Error: already taken");
                }
                // Update game with black player
                GameData updatedGame = new GameData(game.gameID(), game.whiteUsername(), username,
                        game.gameName(), game.game());
                gameDAO.updateGame(updatedGame);
            }
        }
        // If playerColor is null, user is just observing - no update needed
    }
}
