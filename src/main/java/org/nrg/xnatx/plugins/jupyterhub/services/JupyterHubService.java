package org.nrg.xnatx.plugins.jupyterhub.services;

import org.nrg.xft.security.UserI;
import org.nrg.xnatx.plugins.jupyterhub.client.models.Hub;
import org.nrg.xnatx.plugins.jupyterhub.client.models.Server;
import org.nrg.xnatx.plugins.jupyterhub.client.models.Token;
import org.nrg.xnatx.plugins.jupyterhub.client.models.User;

import java.util.List;
import java.util.Optional;

public interface JupyterHubService {

    Hub getVersion();
    Hub getInfo();
    User createUser(UserI user);
    Optional<User> getUser(UserI user);
    List<User> getUsers();
    Optional<Server> getServer(UserI user);
    Optional<Server> getServer(UserI user, String servername);
    void startServer(UserI user, String xsiType, String itemId, String itemLabel, String projectId, String eventTrackingId, String dockerImage);
    void startServer(UserI user, String servername, String xsiType, String itemId, String itemLabel, String projectId, String eventTrackingId, String dockerImage);
    void stopServer(UserI user, String eventTrackingId);
    void stopServer(UserI user, String servername, String eventTrackingId);

    Token createToken(UserI user, String note, Integer expiresIn);

}
