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
    private final UserDAO userDAO = new MemoryUserDAO();
    private final AuthDAO authDAO = new MemoryAuthDAO();
    private final GameDAO gameDAO = new MemoryGameDAO();

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


    }
    private void handleClear(Context ctx) {
        try {
            clearService.clear();
            ctx.status(200);
            ctx.result("{}");
        } catch (DataAccessException e) {
            ctx.status(500);
            ctx.result(gson.toJson(Map.of("message", e.getMessage())));

        }
    }
    private void handleRegister(Context ctx) {
        try {
            RegisterRequest request = gson.fromJson(ctx.body(), RegisterRequest.class);
            RegisterResponse response = userService.register(request);
            ctx.status(200);
            ctx.result(gson.toJson(response));
        } catch (DataAccessException e) {
            if (e.getMessage().equals("Error: bad request")) {
                ctx.status(400);
            } else if (e.getMessage().equals("Error: already taken")) {
                ctx.status(403);
            } else {
                ctx.status(500);
            }
            ctx.result(gson.toJson(Map.of("message", e.getMessage())));  // Use ctx.result()
        }
    }
    private void handleLogin(Context ctx) {
        try {
            LoginRequest request = gson.fromJson(ctx.body(), LoginRequest.class);
            LoginResponse response = userService.login(request);
            ctx.status(200);
            ctx.result(gson.toJson(response));
        } catch (DataAccessException e) {
            if (e.getMessage().equals("Error: bad request")) {
                ctx.status(400);
            } else if (e.getMessage().equals("Error: unauthorized")) {
                ctx.status(401);
            } else {
                ctx.status(500);
            }
            ctx.result(gson.toJson(Map.of("message", e.getMessage())));        }
    }
    private void handleLogout(Context ctx) {
        try {
            String authToken = ctx.header("authorization");
            userService.logout(authToken);
            ctx.status(200);
            ctx.result("{}");
        } catch (DataAccessException e) {
            ctx.status(401);
            ctx.result(gson.toJson(Map.of("message", "Error: unauthorized")));
        }
    }
    private void handleListGames(Context ctx) {
        try {
            String authToken = ctx.header("authorization");
            ListGamesResponse response = gameService.listGames(authToken);
            ctx.status(200);
            ctx.result(gson.toJson(response));
        } catch (DataAccessException e) {
            ctx.status(401);
            ctx.result(gson.toJson(Map.of("message", "Error: unauthorized")));
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
            if (e.getMessage().equals("Error: bad request")) {
                ctx.status(400);
            } else if (e.getMessage().equals("Error: unauthorized")) {
                ctx.status(401);
            } else {
                ctx.status(500);
            }
            ctx.result(gson.toJson(Map.of("message", e.getMessage())));
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
            if (e.getMessage().equals("Error: bad request")) {
                ctx.status(400);
            } else if (e.getMessage().equals("Error: unauthorized")) {
                ctx.status(401);
            } else if (e.getMessage().equals("Error: already taken")) {
                ctx.status(403);
            } else {
                ctx.status(500);
            }
            ctx.result(gson.toJson(Map.of("message", e.getMessage())));
        }
    }


    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

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
