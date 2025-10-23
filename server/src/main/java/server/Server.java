package server;

import io.javalin.*;
import dataaccess.*;
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
    }
    private Object handleClear(Context ctx) {
        try {
            clearService.clear();
            ctx.status(200);
            return "{}";
        } catch (DataAccessException e) {
            ctx.status(500);
            return gson.toJson(Map.of("message", e.getMessage()));
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
