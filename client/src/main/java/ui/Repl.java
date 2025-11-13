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
            String result = preloginClient.eval(line);

            // If the result is "quit", stop the loop
            if (result.equals("quit")) {
                running = false;
            } else {
                // Otherwise, print the result
                System.out.println(result);
            }
        }

        System.out.println("Goodbye!");
    }
}