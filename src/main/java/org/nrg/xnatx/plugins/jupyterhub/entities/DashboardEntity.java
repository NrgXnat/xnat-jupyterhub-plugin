package org.nrg.xnatx.plugins.jupyterhub.entities;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;
import org.nrg.xnatx.plugins.jupyterhub.models.Dashboard;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Table
@Slf4j
public class DashboardEntity extends AbstractHibernateEntity {

    private String name;
    private String description;
    private String framework;
    private Integer port;
    private String command;
    private String fileSource;
    private String gitRepoUrl;
    private String gitRepoBranch;
    private String mainFilePath;

    @ToString.Exclude @EqualsAndHashCode.Exclude private DashboardConfigEntity dashboardConfig;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(length = 4096)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFramework() {
        return framework;
    }

    public void setFramework(String framework) {
        this.framework = framework;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    @Column(length = 4096)
    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getFileSource() {
        return fileSource;
    }

    public void setFileSource(String fileSource) {
        this.fileSource = fileSource;
    }


    public String getGitRepoUrl() {
        return gitRepoUrl;
    }

    public void setGitRepoUrl(String gitRepoUrl) {
        this.gitRepoUrl = gitRepoUrl;
    }

    public String getGitRepoBranch() {
        return gitRepoBranch;
    }

    public void setGitRepoBranch(String gitRepoBranch) {
        this.gitRepoBranch = gitRepoBranch;
    }

    public String getMainFilePath() {
        return mainFilePath;
    }

    public void setMainFilePath(String gitRepoPath) {
        this.mainFilePath = gitRepoPath;
    }

    @OneToOne
    public DashboardConfigEntity getDashboardConfig() {
        return dashboardConfig;
    }

    public void setDashboardConfig(final DashboardConfigEntity dashboardConfig) {
        this.dashboardConfig = dashboardConfig;
    }

    public static DashboardEntity fromPojo(final Dashboard pojo) {
        final DashboardEntity entity = new DashboardEntity();
        entity.update(pojo);
        return entity;
    }

    public Dashboard toPojo() {
        return Dashboard.builder()
                        .id(this.getId())
                        .name(this.getName())
                        .description(this.getDescription())
                        .framework(this.getFramework())
                        .port(this.getPort())
                        .command(this.getCommand())
                        .fileSource(this.getFileSource())
                        .gitRepoUrl(this.getGitRepoUrl())
                        .gitRepoBranch(this.getGitRepoBranch())
                        .mainFilePath(this.getMainFilePath())
                        .build();
    }

    public void update(final Dashboard pojo) {
        // Don't update the ID, that's immutable
        this.setName(pojo.getName());
        this.setDescription(pojo.getDescription());
        this.setFramework(pojo.getFramework());
        this.setPort(pojo.getPort());
        this.setCommand(pojo.getCommand());
        this.setFileSource(pojo.getFileSource());
        this.setGitRepoUrl(pojo.getGitRepoUrl());
        this.setGitRepoBranch(pojo.getGitRepoBranch());
        this.setMainFilePath(pojo.getMainFilePath());
    }
}
