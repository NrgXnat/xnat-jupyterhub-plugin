package org.nrg.xnatx.plugins.jupyterhub.client.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.time.ZonedDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
@Slf4j
public class Token {

    private String token;
    private String id;
    private String user;
    private String kind;
    private String service;
    private List<String> scopes;
    private String note;
    private String oauth_client;
    private String session_id;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss[.SSSSSS]X", timezone = "UTC") private ZonedDateTime expires_at;
    private int expires_in; // lifetime (in seconds) after which the requested token will expire

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss[.SSSSSS]X", timezone = "UTC") private ZonedDateTime created;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss[.SSSSSS]X", timezone = "UTC") private ZonedDateTime last_activity;

}
