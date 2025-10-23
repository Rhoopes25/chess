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
        // TODO: implement
    }
}
