# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- [JHP-95]: Support for XNAT 1.9.0.

### Fixed

- [JHP-103]: Check for xdat_user table before creating JupyterHub user account. 

### Changed: 

- [JHP-112]: The path translation settings have always been applied to both Docker Swarm and Kubernetes deployments. 
             Updated the plugin settings labels and descriptions to reflect this.

## [1.2.0] - 2024-06-27

### Added

- [JHP-82]:  Enables support for JupyterHub named servers. This feature allows users to start multiple Jupyter notebooks
             or Dashboards from the XNAT UI. A new preference has been added to the plugin to limit the number of named
             servers a user can start. The default is 1. May require browser cache to be cleared.
- [JHP-92]:  Adds plugin description, visible in the installed plugins site administration page in XNAT.

### Changed

- [JHP-89]: Changed the action labels to `Start Jupyter Notebook` and `Start Jupyter Dashboard` for clarity and 
            consistency as the batch launch plugin is already contains a 'Processing Dashboard' action.
- [JHP-91]: The 'New / Update Dashboard' modal in the plugin settings accepts both git branches and tags in the 'Branch'
            field. The field label and description have been updated to reflect this ability.

### Fixed

- [JHP-88]: Cleanup the User Authorization table and modal in the plugin preferences page. Usernames were not being 
            sorted alphabetically.
- [JHP-66][] and [XNAT-7867][]: Some file deletions were missed in the previous release.

## [1.1.1] - 2024-05-03

### Fixed

- [JHP-83] - Fix issue with project resources not being mounted. Caused by XNAT-7903.

## [1.1.0] - 2024-03-04

### Added

- [JHP-73][]: Adds dashboards functionality to the JupyterHub plugin. May require browser cache to be cleared.
- [XNAT-7903][] and [JHP-74][]: Support mounting of shared data at the project level. 
- [JHP-70]: Adds capability to configure plugin preferences via environment variables at startup.

### Changed

- [JHP-66][] and [XNAT-7867][]: Moves the compute services code from the plugin to the XNAT core to allow other plugins 
                                to use the compute services. Makes the plugin dependent on XNAT 1.8.10 or later.
- [JHP-77][]: Add GitHub workflows to build the develop branch and publish releases. Move the build process to GitHub 
              Actions and remove the Jenkins build process. Repository has migrated from
              [Bitbucket](https://bitbucket.org/xnatx/xnat-jupyterhub-plugin/) to
              [GitHub](https://github.com/NrgXnat/xnat-jupyterhub-plugin)


## [1.0.1] - 2023-10-12

### Added

- Added changelog.

### Fixed

- [JHP-68][]: Updates date format patterns in the Server, Token, and User class to handle optional fractions of seconds.
- [JHP-69][]: Adds a new preference for JupyterHub Host URL. This preference is used to build the URL for linking users 
              to their Jupyter notebook server. Addresses an issue encountered in multi-node XNAT deployments.
              The new preference will default to the site URL (which will keep the current behavior).

[XNAT-7867]: https://radiologics.atlassian.net/browse/XNAT-7867
[XNAT-7903]: https://radiologics.atlassian.net/browse/XNAT-7903
[JHP-66]: https://radiologics.atlassian.net/jira/software/c/projects/JHP/issues/JHP-66
[JHP-68]: https://radiologics.atlassian.net/jira/software/c/projects/JHP/issues/JHP-68
[JHP-69]: https://radiologics.atlassian.net/jira/software/c/projects/JHP/issues/JHP-69
[JHP-70]: https://radiologics.atlassian.net/jira/software/c/projects/JHP/issues/JHP-70
[JHP-73]: https://radiologics.atlassian.net/jira/software/c/projects/JHP/issues/JHP-73
[JHP-74]: https://radiologics.atlassian.net/jira/software/c/projects/JHP/issues/JHP-74
[JHP-77]: https://radiologics.atlassian.net/jira/software/c/projects/JHP/issues/JHP-77
[JHP-82]: https://radiologics.atlassian.net/jira/software/c/projects/JHP/issues/JHP-82
[JHP-83]: https://radiologics.atlassian.net/jira/software/c/projects/JHP/issues/JHP-83
[JHP-88]: https://radiologics.atlassian.net/jira/software/c/projects/JHP/issues/JHP-88
[JHP-89]: https://radiologics.atlassian.net/jira/software/c/projects/JHP/issues/JHP-89
[JHP-91]: https://radiologics.atlassian.net/jira/software/c/projects/JHP/issues/JHP-91
[JHP-92]: https://radiologics.atlassian.net/jira/software/c/projects/JHP/issues/JHP-92
[JHP-95]: https://radiologics.atlassian.net/jira/software/c/projects/JHP/issues/JHP-95
[JHP-103]: https://radiologics.atlassian.net/jira/software/c/projects/JHP/issues/JHP-103
[JHP-112]: https://radiologics.atlassian.net/jira/software/c/projects/JHP/issues/JHP-112