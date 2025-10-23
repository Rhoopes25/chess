package dataaccess;

import model.GameData;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class MemoryGameDAO implements GameDAO{

    private HashMap<Integer, GameData> games = new HashMap<>();
    private int nextGameID = 1;  // Counter to generate unique IDs

    @Override
    public int createGame(GameData game) throws DataAccessException {
        int gameID = nextGameID++; //gets current ID ad increased by 1

        // The game that comes in has no ID yet, so we make a new GameData with the ID we just made
        // and copy over all the other stuff from the original game
        GameData newGame = new GameData(gameID, game.whiteUsername(),
                game.blackUsername(), game.gameName(), game.game());


        games.put(gameID, newGame); //stores the game
        return gameID;
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        return games.get(gameID);

    }

    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        return games.values();

    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        //replaces game with updated version
        games.put(game.gameID(), game);

    }

    @Override
    public void clear() throws DataAccessException {
        games.clear();
        nextGameID = 1; // Reset the counter
    }
}
