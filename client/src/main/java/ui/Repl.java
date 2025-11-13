package ui;

import java.util.Scanner;

public class Repl {
    private final Scanner scanner;

    public Repl() {
        scanner = new Scanner(System.in);
    }

    public void run() {
        System.out.println("Type 'help' to see available commands.");

        boolean running = true;
        while (running) {
            System.out.print("[LOGGED_OUT] >>> ");
            String line = scanner.nextLine();

            // We'll add command handling here
            if (line.equals("quit")) {
                running = false;
            }
        }

        System.out.println("Goodbye!");
    }
}