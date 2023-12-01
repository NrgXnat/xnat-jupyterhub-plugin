package org.nrg.xnatx.plugins.jupyterhub.models;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Slf4j
public class Dashboard {

    private String name;
    private String description;
    private String framework;
    private String command;
    private String fileSource;
    private String gitRepoUrl;
    private String gitRepoBranch;
    private String mainFilePath;

}
