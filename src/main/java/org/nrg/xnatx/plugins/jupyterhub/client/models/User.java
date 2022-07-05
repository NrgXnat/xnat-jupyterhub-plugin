package org.nrg.xnatx.plugins.jupyterhub.client.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
@Slf4j
public class User {

    private String name;
    private boolean admin;
    private List<String> roles;
    private List<String> groups;
    private String server; // Default, unnamed server
    private String pending;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSX", timezone = "UTC")
    private ZonedDateTime last_activity;

    // Servername -> Server
    private Map<String, Server> servers; // Named servers

    //private String auth_state;

}
