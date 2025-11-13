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