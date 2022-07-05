package org.nrg.xnatx.plugins.jupyterhub.client;

import org.nrg.xnatx.plugins.jupyterhub.client.exceptions.JupyterHubUserNotFoundException;
import org.nrg.xnatx.plugins.jupyterhub.client.exceptions.JupyterServerAlreadyExistsException;
import org.nrg.xnatx.plugins.jupyterhub.client.models.Server;
import org.nrg.xnatx.plugins.jupyterhub.client.models.User;
import org.nrg.xnatx.plugins.jupyterhub.client.models.UserOptions;

import java.util.Optional;

public interface JupyterHubClient {

    User createUser(String username);
    Optional<User> getUser(String username);
    Optional<Server> getServer(String username);
    Optional<Server> getServer(String username, String servername);
    Server startServer(String username, UserOptions userOptions) throws JupyterHubUserNotFoundException, JupyterServerAlreadyExistsException;
    Server startServer(String username, String servername, UserOptions userOptions) throws JupyterHubUserNotFoundException, JupyterServerAlreadyExistsException;
    void stopServer(String username);
    void stopServer(String username, String servername);

}
