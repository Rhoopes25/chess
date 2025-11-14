package client;

import chess.ChessGame;
import ui.BoardDrawer;

public class PostloginClient {
    // This will talk to our server
    private final ServerFacade facade;

    // We need to store the authToken to use for all commands
    private final String authToken;

    // Store the last list of games so we can map game numbers to gameIDs
    private ServerFacade.GameInfo[] lastGamesList = null;


    // Constructor - takes the authToken from login/register
    public PostloginClient(String serverUrl, String authToken) {
        this.facade = new ServerFacade(serverUrl);
        this.authToken = authToken;
    }

    // This holds the result of a command
    public record CommandResult(String message, boolean shouldLogout) {
        // Constructor for simple messages
        public CommandResult(String message) {
            this(message, false);
        }

        // Constructor for logout
        public static CommandResult logout(String message) {
            return new CommandResult(message, true);
        }
    }

    // THE METHODS BELOW ARE OUTSIDE THE RECORD - THEY BELONG TO PostloginClient CLASS

    // This method takes the user's input and figures out what command they want
    public CommandResult eval(String input) {
        // Split the input into words
        var tokens = input.split(" ");

        // The first word is the command
        var command = tokens[0];

        // Everything after the first word are the parameters
        var params = java.util.Arrays.copyOfRange(tokens, 1, tokens.length);

        // Use a switch to call the right method based on the command
        return switch (command) {
            case "create" -> createGame(params);
            case "list" -> listGames();
            case "join" -> joinGame(params);
            case "observe" -> observeGame(params);
            case "logout" -> logout();
            case "help" -> new CommandResult(help());
            default -> new CommandResult("Unknown command. Type 'help' to see available commands.");
        };
    }

    // Create a new game
    // params[0] = game name
    private CommandResult createGame(String[] params) {
        // Check if the user gave us a game name
        if (params.length != 1) {
            return new CommandResult("Error: create requires <name>");
        }

        // Get the game name from parameters
        String gameName = params[0];

        // Try to create the game with the server
        try {
            // Call our ServerFacade createGame method
            var result = facade.createGame(authToken, gameName);

            // If we get here, it worked! Return success message
            return new CommandResult("Created game '" + gameName + "' with ID " + result.gameID());

        } catch (Exception e) {
            // If something went wrong, return the error message
            return new CommandResult("Create game failed: " + e.getMessage());
        }
    }

    // List all games
    private CommandResult listGames() {
        // Try to get the list of games from the server
        try {
            // Call our ServerFacade listGames method
            var result = facade.listGames(authToken);

            // Save the games list so we can reference it later
            lastGamesList = result.games();

            // Check if there are any games
            if (result.games() == null || result.games().length == 0) {
                return new CommandResult("No games available.");
            }

            // Build a string to display all the games
            StringBuilder message = new StringBuilder("Games:\n");

            // Loop through each game and add it to the message
            for (int i = 0; i < result.games().length; i++) {
                var game = result.games()[i];
                // Display as: 1. GameName (White: username, Black: username)
                message.append(String.format("%d. %s (White: %s, Black: %s)\n",
                        i + 1,  // Game number (starting from 1)
                        game.gameName(),
                        game.whiteUsername() != null ? game.whiteUsername() : "empty",
                        game.blackUsername() != null ? game.blackUsername() : "empty"
                ));
            }

            return new CommandResult(message.toString());

        } catch (Exception e) {
            // If something went wrong, return the error message
            return new CommandResult("List games failed: " + e.getMessage());
        }
    }

    // Join a game as a player
// params[0] = game number, params[1] = color (WHITE or BLACK)
    private CommandResult joinGame(String[] params) {
        // Check if the user gave us the right number of parameters
        if (params.length != 2) {
            return new CommandResult("Error: join requires <ID> [WHITE|BLACK]");
        }

        // Validate and get the gameID
        Integer gameID = validateAndGetGameID(params[0]);
        if (gameID == null) {
            return new CommandResult("Error: Invalid game number");
        }

        // Get the color and convert to uppercase
        String color = params[1].toUpperCase();

        // Validate the color
        if (!color.equals("WHITE") && !color.equals("BLACK")) {
            return new CommandResult("Error: Color must be WHITE or BLACK");
        }

        // Try to join the game with the server
        try {
            // Call our ServerFacade joinGame method
            facade.joinGame(authToken, color, gameID);

            // If we get here, it worked!
            // Now draw the board!
            ChessGame game = new ChessGame();  // Create a new game with starting position

            // Draw from the correct perspective based on color
            if (color.equals("WHITE")) {
                BoardDrawer.drawWhiteBoard(game);
            } else {
                BoardDrawer.drawBlackBoard(game);
            }

            return new CommandResult("Joined game as " + color);

        } catch (Exception e) {
            // If something went wrong, return the error message
            return new CommandResult("Join game failed: " + e.getMessage());
        }
    }

    // Observe a game
// params[0] = game number
    private CommandResult observeGame(String[] params) {
        // Check if the user gave us a game number
        if (params.length != 1) {
            return new CommandResult("Error: observe requires <ID>");
        }

        // Validate and get the gameID
        Integer gameID = validateAndGetGameID(params[0]);
        if (gameID == null) {
            return new CommandResult("Error: Invalid game number");
        }

        // For observing, we draw from white's perspective
        ChessGame game = new ChessGame();  // Create a new game with starting position
        BoardDrawer.drawWhiteBoard(game);

        return new CommandResult("Now observing game " + params[0]);
    }

    // Helper method to validate game number and return gameID
    private Integer validateAndGetGameID(String gameNumberStr) {
        // Check if we have a list of games (user must list games first)
        if (lastGamesList == null) {
            return null;
        }

        // Try to parse the game number
        int gameNumber;
        try {
            gameNumber = Integer.parseInt(gameNumberStr);
        } catch (NumberFormatException e) {
            return null;
        }

        // Check if the game number is valid (1-based indexing)
        if (gameNumber < 1 || gameNumber > lastGamesList.length) {
            return null;
        }

        // Get the actual gameID from the list (convert from 1-based to 0-based)
        return lastGamesList[gameNumber - 1].gameID();
    }

    // Logout
    private CommandResult logout() {
        // Try to logout with the server
        try {
            // Call our ServerFacade logout method with our authToken
            facade.logout(authToken);

            // If we get here, it worked! Return a logout result
            return CommandResult.logout("Logged out successfully!");

        } catch (Exception e) {
            // If something went wrong, return the error message
            // But still logout locally (return shouldLogout = true)
            return CommandResult.logout("Logout failed: " + e.getMessage());
        }
    }

    // Show help text
    private String help() {
        return """
Available commands:
  create <name> - Create a new game
  list - List all games
  join <ID> [WHITE|BLACK] - Join a game as a player
  observe <ID> - Observe a game
  logout - Logout
  help - Show this help message
""";
    }
}