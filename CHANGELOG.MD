# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- [JHP-73][]: Adds dashboards functionality to the JupyterHub plugin. May require browser cache to be cleared.
- [XNAT-7903][] and [JHP-74][]: Support mounting of shared data at the project level. 

### Changed

- Requires XNAT 1.8.10 or higher.

## [1.0.1] - 2023-10-12

### Added

- Added changelog.

### Fixed

- [JHP-68][]: Updates date format patterns in the Server, Token, and User class to handle optional fractions of seconds.
- [JHP-69][]: Adds a new preference for JupyterHub Host URL. This preference is used to build the URL for linking users 
              to their Jupyter notebook server. Addresses an issue encountered in multi-node XNAT deployments.
              The new preference will default to the site URL (which will keep the current behavior).


[XNAT-7903]: https://radiologics.atlassian.net/browse/XNAT-7903
[JHP-68]: https://radiologics.atlassian.net/jira/software/c/projects/JHP/issues/JHP-68
[JHP-69]: https://radiologics.atlassian.net/jira/software/c/projects/JHP/issues/JHP-69
[JHP-73]: https://radiologics.atlassian.net/jira/software/c/projects/JHP/issues/JHP-73
[JHP-74]: https://radiologics.atlassian.net/jira/software/c/projects/JHP/issues/JHP-74
