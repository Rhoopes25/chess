package service;

import dataaccess.*;

public class ClearService {
    private final UserDAO userDAO;
    private final AuthDAO authDAO;
    private final GameDAO gameDAO;

    public ClearService(UserDAO userDAO, AuthDAO authDAO, GameDAO gameDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
    }

    // delete CHILD tables first, then PARENT
    public void clear() throws DataAccessException {
        authDAO.clear();   // references users → must go first
        gameDAO.clear();   // may reference users → second
        userDAO.clear();   // parent last
    }
}
