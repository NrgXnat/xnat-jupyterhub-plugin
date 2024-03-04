package org.nrg.xnatx.plugins.jupyterhub.entities;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;
import org.nrg.xnatx.plugins.jupyterhub.models.Dashboard;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.FetchType;

@Entity
@Table(name = "xhbm_dashboard_entity")
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Slf4j
public class DashboardEntity extends AbstractHibernateEntity {

    private String name;
    private String description;
    private String command;
    private String fileSource;
    private String gitRepoUrl;
    private String gitRepoBranch;
    private String mainFilePath;

    @ToString.Exclude @EqualsAndHashCode.Exclude private DashboardConfigEntity dashboardConfig;
    @ToString.Exclude @EqualsAndHashCode.Exclude private DashboardFrameworkEntity dashboardFramework;

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

    @ManyToOne(fetch = FetchType.EAGER)
    public DashboardFrameworkEntity getDashboardFramework() {
        return dashboardFramework;
    }

    public void setDashboardFramework(final DashboardFrameworkEntity dashboardFramework) {
        this.dashboardFramework = dashboardFramework;
    }

    /**
     * Creates a new entity from the given pojo.
     * @param pojo The pojo from which to create the entity.
     * @return The newly created entity.
     */
    public static DashboardEntity fromPojo(final Dashboard pojo) {
        final DashboardEntity entity = new DashboardEntity();
        entity.update(pojo);
        return entity;
    }

    /**
     * Converts the entity to a pojo.
     * @return The pojo representation of the entity.
     */
    public Dashboard toPojo() {
        final String framework = this.getDashboardFramework() != null ? this.getDashboardFramework().getName() : null;

        return Dashboard.builder()
                        .name(this.getName())
                        .description(this.getDescription())
                        .framework(framework)
                        .command(this.getCommand())
                        .fileSource(this.getFileSource())
                        .gitRepoUrl(this.getGitRepoUrl())
                        .gitRepoBranch(this.getGitRepoBranch())
                        .mainFilePath(this.getMainFilePath())
                        .build();
    }

    /**
     * Updates the entity with the values from the given pojo. Doesn't update the ID, that's immutable.
     * @param pojo The pojo from which to update the entity.
     */
    public void update(final Dashboard pojo) {
        // Don't update the ID, that's immutable
        // DashboardFrameworkEntity must be set separately
        this.setName(pojo.getName());
        this.setDescription(pojo.getDescription());
        this.setCommand(pojo.getCommand());
        this.setFileSource(pojo.getFileSource());
        this.setGitRepoUrl(pojo.getGitRepoUrl());
        this.setGitRepoBranch(pojo.getGitRepoBranch());
        this.setMainFilePath(pojo.getMainFilePath());
    }
}
