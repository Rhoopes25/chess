package ui;

import client.PreloginClient;
import client.PostloginClient;
import java.util.Scanner;

public class Repl {
    // Scanner reads input from the keyboard
    private final Scanner scanner;

    // Server URL
    private final String serverUrl = "http://localhost:8080";

    // Constructor - runs when we create a new Repl
    public Repl() {
        scanner = new Scanner(System.in);
    }

    public void run() {
        System.out.println("Type 'help' to see available commands.");

        // Start in the logged out state
        runPrelogin();
    }

    // Handle prelogin state (before user logs in)
    private void runPrelogin() {
        // Create the prelogin client
        var preloginClient = new PreloginClient(serverUrl);

        // Keep looping until user logs in or quits
        boolean running = true;
        while (running) {
            // Print the logged out prompt
            System.out.print("[LOGGED_OUT] >>> ");

            // Read what the user types
            String line = scanner.nextLine();

            // Give the input to the preloginClient
            var result = preloginClient.eval(line);

            // Check if we should quit
            if (result.shouldQuit()) {
                System.out.println("Goodbye!");
                return;  // Exit completely
            }

            // Print the message
            if (!result.message().isEmpty()) {
                System.out.println(result.message());
            }

            // If we got an authToken, switch to postlogin!
            if (result.authToken() != null) {
                runPostlogin(result.authToken());
                // When we return here, user has logged out, so loop continues
            }
        }
    }

    // Handle postlogin state (after user logs in)
    private void runPostlogin(String authToken) {
        // Create the postlogin client with the authToken
        var postloginClient = new PostloginClient(serverUrl, authToken);

        // Keep looping until user logs out
        boolean running = true;
        while (running) {
            // Print the logged in prompt
            System.out.print("[LOGGED_IN] >>> ");

            // Read what the user types
            String line = scanner.nextLine();

            // Give the input to the postloginClient
            var result = postloginClient.eval(line);

            // Print the message
            if (!result.message().isEmpty()) {
                System.out.println(result.message());
            }

            // If we should logout, exit this loop and return to prelogin
            if (result.shouldLogout()) {
                running = false;
            }
        }
    }
}