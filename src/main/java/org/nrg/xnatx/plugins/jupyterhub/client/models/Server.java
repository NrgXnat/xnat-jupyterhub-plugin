package org.nrg.xnatx.plugins.jupyterhub.client.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.time.ZonedDateTime;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
@Slf4j
public class Server {

    private String name;
    private Boolean ready;
    private String pending;
    private String url;
    private String progress_url;
    private Map<String, String> user_options;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss[.SSSSSS]X", timezone = "UTC") private ZonedDateTime started;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss[.SSSSSS]X", timezone = "UTC") private ZonedDateTime last_activity;
}
