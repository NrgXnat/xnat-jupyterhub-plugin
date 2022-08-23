package org.nrg.xnatx.plugins.jupyterhub.client.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ResourceAlreadyExistsException extends Exception {
    public ResourceAlreadyExistsException(final String username, final String servername) {
        super("Jupyter server for user " + username + " with server name " + servername + " is already running. Unable to start a new server.");
    }

    public ResourceAlreadyExistsException(final String username) {
        super("Jupyter server for user " + username + " is already running. Unable to start a new server.");
    }
}
