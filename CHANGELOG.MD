# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Fixed

- [JHP-88]: Cleanup the User Authorization table and modal in the plugin preferences page. Usernames were not being 
            sorted alphabetically.

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
[JHP-83]: https://radiologics.atlassian.net/jira/software/c/projects/JHP/issues/JHP-83
[JHP-88]: https://radiologics.atlassian.net/jira/software/c/projects/JHP/issues/JHP-88
