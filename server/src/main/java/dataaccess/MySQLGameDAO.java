package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;

public class MySQLGameDAO implements GameDAO {

    @Override
    public int createGame(GameData game) throws DataAccessException {
        // First, serialize the ChessGame to JSON
        Gson gson = new Gson();
        String gameJson = gson.toJson(game.game());

        try (var conn = DatabaseManager.getConnection()) {
            String sql = "INSERT INTO games (whiteUsername, blackUsername, gameName, game) VALUES (?, ?, ?, ?)";
            try (var stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, game.whiteUsername());
                stmt.setString(2, game.blackUsername());
                stmt.setString(3, game.gameName());
                stmt.setString(4, gameJson);  // The serialized ChessGame

                stmt.executeUpdate();

                // Get the auto-generated gameID
                var rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);  // Return the generated ID
                }
                throw new DataAccessException("Failed to get generated gameID");
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error creating game: " + e.getMessage());
        }
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            String sql = "SELECT gameID, whiteUsername, blackUsername, gameName, game FROM games WHERE gameID = ?";

            try (var stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, gameID);  // Use setInt for integer!

                var rs = stmt.executeQuery();

                if (rs.next()) {
                    // Read all the columns
                    int foundGameID = rs.getInt("gameID");
                    String whiteUsername = rs.getString("whiteUsername");
                    String blackUsername = rs.getString("blackUsername");
                    String gameName = rs.getString("gameName");
                    String gameJson = rs.getString("game");

                    // Deserialize the JSON back to ChessGame object
                    Gson gson = new Gson();
                    ChessGame chessGame = gson.fromJson(gameJson, ChessGame.class);

                    // Return a GameData object with all fields
                    return new GameData(foundGameID, whiteUsername, blackUsername, gameName, chessGame);
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error getting game: " + e.getMessage());
        }
    }

    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        var games = new ArrayList<GameData>();  // Collection to hold games

        try (var conn = DatabaseManager.getConnection()) {
            String sql = "SELECT gameID, whiteUsername, blackUsername, gameName, game FROM games";

            try (var stmt = conn.prepareStatement(sql)) {
                var rs = stmt.executeQuery();

                while (rs.next()) {
                    int foundGameID = rs.getInt("gameID");
                    String whiteUsername = rs.getString("whiteUsername");
                    String blackUsername = rs.getString("blackUsername");
                    String gameName = rs.getString("gameName");
                    String gameJson = rs.getString("game");

                    // Deserialize the JSON back to ChessGame object
                    Gson gson = new Gson();
                    ChessGame chessGame = gson.fromJson(gameJson, ChessGame.class);

                    // Create a GameData object and add it to the list
                    GameData gameData = new GameData(foundGameID, whiteUsername, blackUsername, gameName, chessGame);
                    games.add(gameData);
                }

                return games;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error listing games: " + e.getMessage());
        }
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        Gson gson = new Gson();
        String gameJson = gson.toJson(game.game());

        try (var conn = DatabaseManager.getConnection()) {
            String sql = "UPDATE games SET whiteUsername = ?, blackUsername = ?, gameName = ?, game = ? WHERE gameID = ?";
            try (var stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, game.whiteUsername());
                stmt.setString(2, game.blackUsername());
                stmt.setString(3, game.gameName());
                stmt.setString(4, gameJson);
                stmt.setInt(5, game.gameID());

                stmt.executeUpdate();

                }
        } catch (SQLException e) {
            throw new DataAccessException("Error creating game: " + e.getMessage());
        }
    }

    @Override
    public void clear() throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement("DELETE FROM games")) {
                preparedStatement.executeUpdate();
            }

        } catch (SQLException e) {
            throw new DataAccessException("Error clearing games: " + e.getMessage());
        }
    }
}
