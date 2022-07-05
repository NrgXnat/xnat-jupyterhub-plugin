package org.nrg.xnatx.plugins.jupyterhub.client.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class JupyterHubUserNotFoundException extends Exception {
    public JupyterHubUserNotFoundException(String username) {
        super("Jupyter user " + username + " not found.");
    }
}
