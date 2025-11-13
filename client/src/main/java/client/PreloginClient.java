package client;

public class PreloginClient {
    // This will talk to our server
    private final ServerFacade facade;

    // Constructor: this runs when we create a new PreloginClient
    public PreloginClient(String serverUrl) {
        // Create a ServerFacade so we can make HTTP requests
        this.facade = new ServerFacade(serverUrl);


    }
    // This method takes the user's input and figures out what command they want
    public String eval(String input) {
        // Split the input into words
        var tokens = input.split(" ");

        // The first word is the command (like "register" or "login")
        var command = tokens[0];

        // Everything after the first word are the parameters
        var params = java.util.Arrays.copyOfRange(tokens, 1, tokens.length);

        //  Switch to call the right method based on the command
        return switch (command) {
            case "register" -> register(params);
            case "login" -> login(params);
            case "quit" -> "quit";
            case "help" -> help();
            default -> "Unknown command. Type 'help' to see available commands.";
        };
    }

    // This method will handle the register command
    private String register(String[] params) {
        // We need: username, password, email (3 things)
        if (params.length != 3) {
            return "Error: register requires <username> <password> <email>";
        }

        // Get the parameters from the array
        String username = params[0];
        String password = params[1];
        String email = params[2];

        // Try to register with the server
        try {
            // Call our ServerFacade register method
            var result = facade.register(username, password, email);

            // If we get here, it worked! Return success message
            return "Registered successfully as " + result.username() + "!";

        } catch (Exception e) {
            // If something went wrong, return the error message
            return "Register failed: " + e.getMessage();
        }
    }

    // This method will handle the login command
    private String login(String[] params) {
        return "Login not implemented yet";
    }

    private String help() {
        // Return a string that shows all available commands
        return """
        Available commands:
          register <username> <password> <email> - Create a new account
          login <username> <password> - Login to your account
          quit - Exit the program
          help - Show this help message
        """;
    }
}