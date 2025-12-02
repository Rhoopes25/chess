package server;

import io.javalin.*;
import dataaccess.*;
import model.*;
import service.*;
import io.javalin.http.Context;
import com.google.gson.Gson;
import java.util.Map;

public class Server {

    private final Javalin javalin;
    // converts java objects into JSON
    private final Gson gson = new Gson();

    // DAOs
    private final UserDAO userDAO = new MySQLUserDAO();
    private final AuthDAO authDAO = new MySQLAuthDAO();
    private final GameDAO gameDAO = new MySQLGameDAO();
    // Services
    private final ClearService clearService;
    private final UserService userService;
    private final GameService gameService;



    // This method will register URL routes with Javalin
    // like routing
    private void registerEndpoints() {
        javalin.delete("/db", this::handleClear);
        javalin.post("/user", this::handleRegister);
        javalin.post("/session", this::handleLogin);
        javalin.delete("/session", this::handleLogout);
        javalin.get("/game", this::handleListGames);
        javalin.post("/game", this::handleCreateGame);
        javalin.put("/game", this::handleJoinGame);

        // WebSocket endpoint - create ONE shared handler for ALL connections
        var wsHandler = new server.websocket.WebSocketHandler(authDAO, gameDAO);

        javalin.ws("/ws", ws -> {
            ws.onConnect(ctx -> {
                ctx.enableAutomaticPings();  // THIS IS THE KEY LINE!
                wsHandler.onOpen(ctx.session);
            });

            ws.onMessage(ctx -> {
                try {
                    wsHandler.onMessage(ctx.session, ctx.message());
                } catch (Exception e) {
                    System.err.println("WebSocket error: " + e.getMessage());
                }
            });

            ws.onClose(ctx -> {
                wsHandler.onClose(ctx.session, ctx.status(), ctx.reason());
            });

            ws.onError(ctx -> {
                wsHandler.onError(ctx.session, ctx.error());
            });
        });
    }

    // Helper method to handle errors and set appropriate status codes
    private void handleError(Context ctx, DataAccessException e) {
        String message = e.getMessage();

        if ("Error: bad request".equals(message)) {
            ctx.status(400);
        } else if ("Error: unauthorized".equals(message)) {
            ctx.status(401);
        } else if ("Error: already taken".equals(message)) {
            ctx.status(403);
        } else {
            // Any other DataAccessException is an Internal Server Error.
            ctx.status(500);
            // Ensure the body contains the word "Error" (the test lower-cases and looks for "error")
            if (message == null || !message.toLowerCase().contains("error")) {
                message = "Error: description";
            }
        }

        ctx.result(gson.toJson(Map.of("message", message)));
    }

    private String authHeader(Context ctx) {
        var t = ctx.header("authorization");
        return (t != null) ? t : ctx.header("Authorization");
    }


    private void handleClear(Context ctx) {
        try {
            clearService.clear();
            ctx.status(200);
            ctx.result("{}");
        } catch (DataAccessException e) {
            // was: ctx.status(500) + raw message
            handleError(ctx, e);
        } catch (Exception e) {
            ctx.status(500);
            ctx.result(gson.toJson(Map.of("message", "Error: description")));
        }
    }


    private void handleRegister(Context ctx) {
        try {
            RegisterRequest request = gson.fromJson(ctx.body(), RegisterRequest.class);
            RegisterResponse response = userService.register(request);
            ctx.status(200);
            ctx.result(gson.toJson(response));
        } catch (DataAccessException e) {
            handleError(ctx, e);
        }
    }

    private void handleLogin(Context ctx) {
        try {
            var request = gson.fromJson(ctx.body(), LoginRequest.class);
            if (request == null) { // malformed or empty JSON
                throw new DataAccessException("Error: bad request");
            }
            var response = userService.login(request);
            ctx.status(200);
            ctx.result(gson.toJson(response));

        } catch (IllegalArgumentException e) {
            // If BCrypt.checkpw sees a non-hash in DB, treat like bad creds
            handleError(ctx, new DataAccessException("Error: unauthorized"));

        } catch (DataAccessException e) {
            handleError(ctx, e);

        } catch (Exception e) {
            ctx.status(500);
            ctx.result(gson.toJson(Map.of("message", "Error: description")));
        }
    }



    private void handleLogout(Context ctx) {
        try {
            String authToken = authHeader(ctx); // see helper below
            userService.logout(authToken);
            ctx.status(200);
            ctx.result("{}");
        } catch (DataAccessException e) {
            // was: always 401
            handleError(ctx, e);
        } catch (Exception e) {
            ctx.status(500);
            ctx.result(gson.toJson(Map.of("message", "Error: description")));
        }
    }


    private void handleListGames(Context ctx) {
        try {
            String authToken = authHeader(ctx);
            ListGamesResponse response = gameService.listGames(authToken);
            ctx.status(200);
            ctx.result(gson.toJson(response));
        } catch (DataAccessException e) {
            // was: always 401
            handleError(ctx, e);
        } catch (Exception e) {
            ctx.status(500);
            ctx.result(gson.toJson(Map.of("message", "Error: description")));
        }
    }


    private void handleCreateGame(Context ctx) {
        try {
            String authToken = ctx.header("authorization");
            CreateGameRequest request = gson.fromJson(ctx.body(), CreateGameRequest.class);
            CreateGameResponse response = gameService.createGame(request, authToken);
            ctx.status(200);
            ctx.result(gson.toJson(response));
        } catch (DataAccessException e) {
            handleError(ctx, e);
        }
    }

    private void handleJoinGame(Context ctx) {
        try {
            String authToken = ctx.header("authorization");
            JoinGameRequest request = gson.fromJson(ctx.body(), JoinGameRequest.class);
            gameService.joinGame(request, authToken);
            ctx.status(200);
            ctx.result("{}");
        } catch (DataAccessException e) {
            handleError(ctx, e);
        }
    }


    public Server() {
        javalin = Javalin.create(config -> {
            config.staticFiles.add("web");
            config.jetty.modifyServer(server -> {
                server.setStopTimeout(0);
            });
        });

        try {
            DatabaseManager.initializeTables();
        } catch (DataAccessException e) {
            throw new RuntimeException("Failed to initialize database", e);
        }

        // Initialize services with DAOs
        clearService = new ClearService(userDAO, authDAO, gameDAO);
        userService = new UserService(userDAO, authDAO);
        gameService = new GameService(authDAO, gameDAO);

        registerEndpoints();
    }


    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }


    public void stop() {
        javalin.stop();
    }
}