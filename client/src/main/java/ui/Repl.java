package ui;

import client.PreloginClient;
import java.util.Scanner;

public class Repl {
    // Scanner reads input from the keyboard
    private final Scanner scanner;

    // Handles all the prelogin commands
    private final PreloginClient preloginClient;

    // Constructor - runs when we create a new Repl
    public Repl() {
        scanner = new Scanner(System.in);
        preloginClient = new PreloginClient("http://localhost:8080");
    }

    public void run() {
        System.out.println("Type 'help' to see available commands.");

        // Keep looping until the user quits
        boolean running = true;
        while (running) {
            // Print the prompt
            System.out.print("[LOGGED_OUT] >>> ");

            // Read what the user types
            String line = scanner.nextLine();

            // Give the input to the preloginClient and get back a result
            var result = preloginClient.eval(line);

            // If the result is "quit", stop the loop
            if (result.shouldQuit()) {
                running = false;
            } else {
                // Otherwise, print the result
                if (!result.message().isEmpty()) {
                    System.out.println(result.message());
                }
            }
        }

        System.out.println("Goodbye!");
    }
}