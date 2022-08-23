package org.nrg.xnatx.plugins.jupyterhub.client;

import org.nrg.xnatx.plugins.jupyterhub.client.exceptions.ResourceAlreadyExistsException;
import org.nrg.xnatx.plugins.jupyterhub.client.exceptions.UserNotFoundException;
import org.nrg.xnatx.plugins.jupyterhub.client.models.Server;
import org.nrg.xnatx.plugins.jupyterhub.client.models.User;
import org.nrg.xnatx.plugins.jupyterhub.client.models.UserOptions;

import java.util.Optional;

public interface JupyterHubClient {

    User createUser(String username);
    Optional<User> getUser(String username);
    Optional<Server> getServer(String username);
    Optional<Server> getServer(String username, String servername);
    void startServer(String username, UserOptions userOptions) throws UserNotFoundException, ResourceAlreadyExistsException;
    void startServer(String username, String servername, UserOptions userOptions) throws UserNotFoundException, ResourceAlreadyExistsException;
    void stopServer(String username);
    void stopServer(String username, String servername);

}
