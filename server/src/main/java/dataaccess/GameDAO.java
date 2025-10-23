package dataaccess;

import model.GameData;

import java.util.Collection;

public interface GameDAO {
    // creates game returns the gameID
    int createGame(GameData game) throws DataAccessException;

    // gets a game by ID
    GameData getGame(int gameID) throws DataAccessException;

    //returns all games
    Collection<GameData> listGames() throws DataAccessException;

    //updates an existing game
    void updateGame(GameData game) throws DataAccessException;

    // clears all games
    void clear() throws DataAccessException;
}
