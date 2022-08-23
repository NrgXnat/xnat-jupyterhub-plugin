package org.nrg.xnatx.plugins.jupyterhub.client;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xnatx.plugins.jupyterhub.client.exceptions.ResourceAlreadyExistsException;
import org.nrg.xnatx.plugins.jupyterhub.client.exceptions.UserNotFoundException;
import org.nrg.xnatx.plugins.jupyterhub.client.models.Server;
import org.nrg.xnatx.plugins.jupyterhub.client.models.User;
import org.nrg.xnatx.plugins.jupyterhub.client.models.UserOptions;
import org.springframework.http.*;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;

@Slf4j
public class DefaultJupyterHubClient implements JupyterHubClient {

    private final String jupyterHubApiToken;
    private final String jupyterHubApiUrl;

    public DefaultJupyterHubClient(final String jupyterHubApiToken, final String jupyterHubUrl) {
        this.jupyterHubApiToken = jupyterHubApiToken;
        this.jupyterHubApiUrl = jupyterHubUrl + "/hub/api";
    }

    @Override
    public User createUser(final String username) {
        log.debug("Creating JupyterHub user {}", username);

        RestTemplate restTemplate = new RestTemplate();
        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "token " + jupyterHubApiToken);
        HttpEntity<String> request = new HttpEntity<>(null, headers);

        try {
            ResponseEntity<User> response = restTemplate.exchange(userUrl(username),
                                                                  HttpMethod.POST,
                                                                  request, User.class);

            log.info("JupyterHub user {} created", username);

            return response.getBody();
        } catch (RestClientException e) {
            log.error("Unable to create user on JupyterHub", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<User> getUser(String username) {
        log.debug("Getting JupyterHub user {}", username);

        RestTemplate restTemplate = new RestTemplate();

        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "token " + jupyterHubApiToken);
        HttpEntity<String> request = new HttpEntity<>(null, headers);

        try {
            ResponseEntity<User> response = restTemplate.exchange(userUrl(username),
                                                                  HttpMethod.GET,
                                                                  request, User.class);

            log.debug("JupyterHub user {} retrieved", username);
            return Optional.of(response.getBody());
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                log.debug("User {} does not exist on JupyterHub", username);
                return Optional.empty();
            } else {
                log.error("Unable to get user " + username + " from JupyterHub", e);
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public Optional<Server> getServer(String username) {
        return getServer(username, "");
    }

    @Override
    public Optional<Server> getServer(String username, String servername) {
        log.debug("Getting server {} for user {}", servername, username);
        Optional<User> user = getUser(username);

        if (user.isPresent()) {
            Map<String, Server> servers = user.get().getServers();
            return Optional.ofNullable(servers.get(servername));
        } else {
            log.debug("Server {} for user {} not found", servername, username);
            return Optional.empty();
        }
    }

    @Override
    public void startServer(String username, UserOptions userOptions) throws UserNotFoundException, ResourceAlreadyExistsException {
        startServer(username, "", userOptions);
    }

    @Override
    public void startServer(String username, String servername, UserOptions userOptions) throws UserNotFoundException, ResourceAlreadyExistsException {
        log.debug("User {} is trying to start server {} with user options {}", username, servername, userOptions);

        // Check if user exists in JupyterHub
        User user = getUser(username).orElseThrow(() -> new UserNotFoundException(username));

        // Check if server is already running
        Optional<Server> server = Optional.ofNullable(user.getServers().get(servername));
        if (server.isPresent()) {
            log.error("Cannot start Jupyter Server {} for user {}. Server is already running", servername, username);
            if (StringUtils.isBlank(servername))
                throw new ResourceAlreadyExistsException(username);
            else {
                throw new ResourceAlreadyExistsException(username, servername);
            }
        }

        // User exist and server does not. Let's start a new server.
        RestTemplate restTemplate = new RestTemplate();

        // Create request and add XNAT service authorization token to request header
        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "token " + jupyterHubApiToken);
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        HttpEntity<UserOptions> request = new HttpEntity<>(userOptions, headers);

        try {
            // POST server request to JupyterHub
            ResponseEntity<String> response = restTemplate.exchange(serverUrl(username, servername),
                                                                    HttpMethod.POST,
                                                                    request, String.class);

            if (response.getStatusCodeValue() >= 200 && response.getStatusCodeValue() <= 299) {
                return;
            } else {
                final String msg = "Failed to start Jupyter Server " + servername +
                        " for user " + username +
                        " response: " + response;
                log.error(msg);
                throw new RuntimeException(msg);
            }
        } catch (RestClientException e) {
            log.error("Failed to start Jupyter Server " + servername + " for user " + username, e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stopServer(String username) {
        stopServer(username, "");
    }

    @Override
    public void stopServer(String username, String servername) {
        log.debug("User {} is trying to stop server {}", username, servername);

        RestTemplate restTemplate = new RestTemplate();

        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "token " + jupyterHubApiToken);
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        HttpEntity<String> request = new HttpEntity<>(null, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(serverUrl(username, servername),
                                                                  HttpMethod.DELETE,
                                                                  request, String.class);

            log.debug("JupyterHub server {} for user {} stopped", servername, username);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                log.debug("User {} / Server {} not found.", username, servername);
            } else {
                log.error("Failed to stop Jupyter server", e);
                throw e;
            }
        }
    }

    private String serverUrl(final String username, final String servername) {
        if (StringUtils.isBlank(servername)) {
            return jupyterHubApiUrl + "/users/" + username + "/server";
        } else {
            return jupyterHubApiUrl + "/users/" + username + "/servers/" + servername;
        }
    }

    private String userUrl(final String username) {
        return jupyterHubApiUrl + "/users/" + username;
    }
}
