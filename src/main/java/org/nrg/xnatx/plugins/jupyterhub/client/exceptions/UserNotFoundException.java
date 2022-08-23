package org.nrg.xnatx.plugins.jupyterhub.client.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class UserNotFoundException extends Exception {
    public UserNotFoundException(String username) {
        super("JupyterHub user " + username + " not found.");
    }
}
