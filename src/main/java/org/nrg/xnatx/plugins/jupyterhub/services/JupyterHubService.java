package org.nrg.xnatx.plugins.jupyterhub.services;

import org.nrg.xft.security.UserI;
import org.nrg.xnatx.plugins.jupyterhub.client.models.Server;
import org.nrg.xnatx.plugins.jupyterhub.client.models.User;

import java.util.Optional;

public interface JupyterHubService {

    User createUser(UserI user);
    Optional<User> getUser(UserI user);
    Optional<Server> getServer(UserI user);
    Optional<Server> getServer(UserI user, String servername);
    void startServer(UserI user, String xsiType, String itemId, String projectId, String eventTrackingId);
    void startServer(UserI user, String servername, String xsiType, String itemId, String projectId, String eventTrackingId);
    void stopServer(UserI user, String eventTrackingId);
    void stopServer(UserI user, String servername, String eventTrackingId);

}
