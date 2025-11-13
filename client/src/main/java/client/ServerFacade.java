package client;

import com.google.gson.Gson;
import java.net.HttpURLConnection;
import java.net.URI;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class ServerFacade {
    private final String serverUrl;

    public ServerFacade(String url) {
        this.serverUrl = url;
    }

    public ServerFacade(int port) {
        this.serverUrl = "http://localhost:" + port;
    }

    // Request/Response records
    public record RegisterRequest(String username, String password, String email) {
    }

    public record RegisterResult(String username, String authToken) {
    }

    // Register method
    public RegisterResult register(String username, String password, String email) throws Exception {
        // Create the request object
        var request = new RegisterRequest(username, password, email);

        // Create the URL
        var url = serverUrl + "/user";

        // Open Connection
        HttpURLConnection connection = (HttpURLConnection) new URI(url).toURL().openConnection();

        // Set HTTP method
        connection.setRequestMethod("POST");

        //Set Headers
        connection.setRequestProperty("Content-Type", "application/json");

        // Enable output (sending data)
        connection.setDoOutput(true);

        // Convert request object to JSON and send it
        try (OutputStream requestBody = connection.getOutputStream()) {
            var jsonBody = new Gson().toJson(request);
            requestBody.write(jsonBody.getBytes());
        }

        // Check if request was successful
        if (connection.getResponseCode() == 200) {
            // Success! Read the response
            try (InputStream responseBody = connection.getInputStream()) {
                InputStreamReader reader = new InputStreamReader(responseBody);
                return new Gson().fromJson(reader, RegisterResult.class);
            }
        } else {
            // Error! Read the error message
            try (InputStream errorBody = connection.getErrorStream()) {
                InputStreamReader reader = new InputStreamReader(errorBody);
                //  exception
                throw new Exception("Register failed: " + connection.getResponseCode());
            }

        }

    }

    // Login
    public record LoginRequest(String username, String password) {}
    public record LoginResult(String username, String authToken) {}
    public LoginResult login(String username, String password) throws Exception {
        // Create the request object
        var request = new LoginRequest(username, password);

        // Create the URL
        var url = serverUrl + "/session";

        // Open connection
        HttpURLConnection connection = (HttpURLConnection) new URI(url).toURL().openConnection();

        // Set HTTP method
        connection.setRequestMethod("POST");

        // Set headers
        connection.setRequestProperty("Content-Type", "application/json");

        // Enable output
        connection.setDoOutput(true);

        // Send request
        try (OutputStream requestBody = connection.getOutputStream()) {
            var jsonBody = new Gson().toJson(request);
            requestBody.write(jsonBody.getBytes());
        }

        // Handle response
        if (connection.getResponseCode() == 200) {
            try (InputStream responseBody = connection.getInputStream()) {
                InputStreamReader reader = new InputStreamReader(responseBody);
                return new Gson().fromJson(reader, LoginResult.class);
            }
        } else {
            throw new Exception("Login failed: " + connection.getResponseCode());
        }
    }
    // Create Game
    public record CreateGameRequest(String gameName) {}
    public record CreateGameResult(int gameID) {}
    public CreateGameResult createGame(String authToken, String gameName) throws Exception {
        // Create the request object
        var request = new CreateGameRequest(gameName);

        // Create the URL
        var url = serverUrl + "/game";

        // Open connection
        HttpURLConnection connection = (HttpURLConnection) new URI(url).toURL().openConnection();

        // Set HTTP method
        connection.setRequestMethod("POST");

        // Set headers
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", authToken);  // <-- NEW! Auth header

        // Enable output
        connection.setDoOutput(true);

        // Send request
        try (OutputStream requestBody = connection.getOutputStream()) {
            var jsonBody = new Gson().toJson(request);
            requestBody.write(jsonBody.getBytes());
        }

        // Handle response
        if (connection.getResponseCode() == 200) {
            try (InputStream responseBody = connection.getInputStream()) {
                InputStreamReader reader = new InputStreamReader(responseBody);
                return new Gson().fromJson(reader, CreateGameResult.class);
            }
        } else {
            throw new Exception("Create game failed: " + connection.getResponseCode());
        }
    }
    // List Games
    public record ListGamesResult(GameInfo[] games) {}
    public record GameInfo(int gameID, String whiteUsername, String blackUsername, String gameName) {}
    public ListGamesResult listGames(String authToken) throws Exception {
        // Create the URL
        var url = serverUrl + "/game";

        // Open connection
        HttpURLConnection connection = (HttpURLConnection) new URI(url).toURL().openConnection();

        // Set HTTP method
        connection.setRequestMethod("GET");

        // Set headers (only Authorization needed)
        connection.setRequestProperty("Authorization", authToken);

        // NO setDoOutput(true) - we're not sending a body
        // NO request body for GET requests;)

        // Handle response
        if (connection.getResponseCode() == 200) {
            try (InputStream responseBody = connection.getInputStream()) {
                InputStreamReader reader = new InputStreamReader(responseBody);
                return new Gson().fromJson(reader, ListGamesResult.class);
            }
        } else {
            throw new Exception("List games failed: " + connection.getResponseCode());
        }
    }
    // Join Game
    public record JoinGameRequest(String playerColor, int gameID) {}
    public void joinGame(String authToken, String playerColor, int gameID) throws Exception {
        // Create the request object
        var request = new JoinGameRequest(playerColor, gameID);

        // Create the URL
        var url = serverUrl + "/game";

        // Open connection
        HttpURLConnection connection = (HttpURLConnection) new URI(url).toURL().openConnection();

        // Set HTTP method
        connection.setRequestMethod("PUT");  // PUT to update/join game

        // Set headers
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", authToken);

        // Enable output
        connection.setDoOutput(true);

        // Send request
        try (OutputStream requestBody = connection.getOutputStream()) {
            var jsonBody = new Gson().toJson(request);
            requestBody.write(jsonBody.getBytes());
        }

        // Handle response
        if (connection.getResponseCode() == 200) {
            // Success! Join game returns empty body (just 200 OK)
            return;
        } else {
            throw new Exception("Join game failed: " + connection.getResponseCode());
        }
    }
    public void logout(String authToken) throws Exception {
        // Create the URL
        var url = serverUrl + "/session";

        // Open connection
        HttpURLConnection connection = (HttpURLConnection) new URI(url).toURL().openConnection();

        // Set HTTP method
        connection.setRequestMethod("DELETE");  // DELETE to remove session

        // Set headers (only Authorization needed)
        connection.setRequestProperty("Authorization", authToken);

        // NO request body for DELETE

        // Handle response
        if (connection.getResponseCode() == 200) {
            // Success! Logout returns empty body
            return;
        } else {
            throw new Exception("Logout failed: " + connection.getResponseCode());
        }
    }
    public void clear() throws Exception {
        // Create the URL
        var url = serverUrl + "/db";

        // Open connection
        HttpURLConnection connection = (HttpURLConnection) new URI(url).toURL().openConnection();

        // Set HTTP method
        connection.setRequestMethod("DELETE");

        // NO headers needed
        // NO request body

        // Handle response
        if (connection.getResponseCode() == 200) {
            // Success! Clear returns empty body
            return;
        } else {
            throw new Exception("Clear failed: " + connection.getResponseCode());
        }
    }

}