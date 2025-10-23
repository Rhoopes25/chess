package server;

import io.javalin.*;
import dataaccess.*;
import service.*;


public class Server {

    private final Javalin javalin;

    // DAOs
    private final UserDAO userDAO = new MemoryUserDAO();
    private final AuthDAO authDAO = new MemoryAuthDAO();
    private final GameDAO gameDAO = new MemoryGameDAO();

    // Services
    private final ClearService clearService;
    private final UserService userService;
    private final GameService gameService;


    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        // Initialize services with DAOs
        clearService = new ClearService(userDAO, authDAO, gameDAO);
        userService = new UserService(userDAO, authDAO);
        gameService = new GameService(authDAO, gameDAO);
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
