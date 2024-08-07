package org.nrg.xnatx.plugins.jupyterhub.client;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xnatx.plugins.jupyterhub.client.exceptions.ResourceAlreadyExistsException;
import org.nrg.xnatx.plugins.jupyterhub.client.exceptions.UserNotFoundException;
import org.nrg.xnatx.plugins.jupyterhub.client.models.*;
import org.springframework.http.*;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class DefaultJupyterHubClient implements JupyterHubClient {

    private final String jupyterHubApiToken;
    private final String jupyterHubApiUrl;

    public DefaultJupyterHubClient(final String jupyterHubApiToken, final String jupyterHubApiUrl) {
        this.jupyterHubApiToken = jupyterHubApiToken;
        this.jupyterHubApiUrl = jupyterHubApiUrl;
    }

    /**
     * Gets the JupyterHub version. Per JupyterHub documentation: This endpoint is not authenticated for the purpose of
     * clients and user to identify the JupyterHub version before setting up authentication.
     * <p>
     * JupyterHub API endpoint: /
     *
     * @return Hub with version number only
     */
    @Override
    public Hub getVersion() {
        log.trace("Getting JupyterHub version");

        RestTemplate restTemplate = new RestTemplate();
        // Skip authentication
        HttpEntity<String> request = new HttpEntity<>(null, null);

        try {
            ResponseEntity<Hub> response = restTemplate.exchange(versionUrl(),
                                                                 HttpMethod.GET,
                                                                 request, Hub.class);
            log.trace("JupyterHub version received.");
            return response.getBody();
        } catch (Exception e) {
            log.debug("Unable to get JupyterHub version", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets detailed JupyterHub information, including Python version, JupyterHub's version and executable path, and
     * which Authenticator and Spawner are active.
     * <p>
     * JupyterHub API endpoint: /info
     *
     * @return Hub with version number only
     */
    @Override
    public Hub getInfo() {
        log.trace("Getting JupyterHub info");

        RestTemplate restTemplate = new RestTemplate();
        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "token " + jupyterHubApiToken);
        HttpEntity<String> request = new HttpEntity<>(null, headers);

        try {
            ResponseEntity<Hub> response = restTemplate.exchange(infoUrl(),
                                                                 HttpMethod.GET,
                                                                 request, Hub.class);
            log.trace("JupyterHub info received.");
            return response.getBody();
        } catch (RestClientException e) {
            throw new RuntimeException(e);
        }
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
            log.debug("Unable to create user on JupyterHub", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Get list of JupyterHub users.
     *
     * @return The Hub's user list
     */
    @Override
    public List<User> getUsers() {
        log.debug("Getting all users from JupyterHub");

        RestTemplate restTemplate = new RestTemplate();
        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "token " + jupyterHubApiToken);
        HttpEntity<String> request = new HttpEntity<>(null, headers);

        try {
            ResponseEntity<User[]> response = restTemplate.exchange(usersUrl(),
                                                                  HttpMethod.GET,
                                                                  request, User[].class);

            return Arrays.asList(response.getBody());
        } catch (RestClientException e) {
            log.debug("Unable to get users from JupyterHub", e);
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
                log.debug("Unable to get user " + username + " from JupyterHub", e);
                throw new RuntimeException(e);
            }
        } catch (Exception e) {
            log.debug("Unable to get user " + username + " from JupyterHub", e);
            throw new RuntimeException(e);
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
            log.debug("Failed to start Jupyter Server " + servername + " for user " + username, e);
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

        Map<String, Boolean> requestBody = Collections.singletonMap("remove", true);

        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "token " + jupyterHubApiToken);
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        HttpEntity<Map<String, Boolean>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(serverUrl(username, servername),
                                                                  HttpMethod.DELETE,
                                                                  request, String.class);

            log.debug("JupyterHub server {} for user {} stopped", servername, username);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                log.debug("User {} / Server {} not found.", username, servername);
            } else {
                log.debug("Failed to stop Jupyter server", e);
                throw e;
            }
        }
    }

    /**
     * Create a new token for the user
     * <p>
     * JupyterHub API endpoint: /users/{name}/tokens
     *
     * @param username The user to create the token for.
     * @param token Token with expires_in, note, and scopes defined.
     * @return The newly created token.
     */
    @Override
    public Token createToken(String username, Token token) {
        log.debug("Creating token for user {}", username);

        RestTemplate restTemplate = new RestTemplate();

        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "token " + jupyterHubApiToken);
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        HttpEntity<Token> request = new HttpEntity<>(token, headers);

        try {
            ResponseEntity<Token> response = restTemplate.exchange(tokenUrl(username, null),
                                                                    HttpMethod.POST,
                                                                    request, Token.class);

            log.debug("Token created for user {}", username);
            return response.getBody();
        } catch (Exception e) {
            log.debug("Unable to create token for user " + username, e);
            throw new RuntimeException(e);
        }
    }

    private String serverUrl(final String username, final String servername) {
        if (StringUtils.isBlank(servername)) {
            return jupyterHubApiUrl + "/users/" + username + "/server";
        } else {
            return jupyterHubApiUrl + "/users/" + username + "/servers/" + servername;
        }
    }

    private String usersUrl() {
        return jupyterHubApiUrl + "/users";
    }

    private String userUrl(final String username) {
        return jupyterHubApiUrl + "/users/" + username;
    }

    private String versionUrl() {
        return jupyterHubApiUrl + "/";
    }

    private String infoUrl() {
        return jupyterHubApiUrl + "/info";
    }

    private String tokenUrl(final String username, final String tokenId) {
        if (StringUtils.isBlank(tokenId)) {
            return jupyterHubApiUrl + "/users/" + username + "/tokens";
        } else {
            return jupyterHubApiUrl + "/users/" + username + "/tokens/" + tokenId;
        }
    }
}
