package client;

public class PostloginClient {
    // This will talk to our server
    private final ServerFacade facade;

    // We need to store the authToken to use for all commands
    private final String authToken;

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
        return new CommandResult("Create game not implemented yet");
    }

    // List all games
    private CommandResult listGames() {
        return new CommandResult("List games not implemented yet");
    }

    // Join a game as a player
    // params[0] = game number, params[1] = color (WHITE or BLACK)
    private CommandResult joinGame(String[] params) {
        return new CommandResult("Join game not implemented yet");
    }

    // Observe a game
    // params[0] = game number
    private CommandResult observeGame(String[] params) {
        return new CommandResult("Observe game not implemented yet");
    }

    // Logout
    private CommandResult logout() {
        return new CommandResult("Logout not implemented yet");
    }

    // Show help text
    // Show help text
    // Show help text
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