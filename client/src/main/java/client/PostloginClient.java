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
}