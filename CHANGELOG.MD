# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.1] - 2023-10-12

### Features
- Added changelog.

### Fixed
- [JHP-68][]: Updates date format patterns in the Server, Token, and User class to handle optional fractions of seconds.
- [JHP-69][]: Adds a new preference for JupyterHub Host URL. This preference is used to build the URL for linking users 
              to their Jupyter notebook server. Addresses an issue encountered in multi-node XNAT deployments.
              The new preference will default to the site URL (which will keep the current behavior).

[JHP-68]: https://radiologics.atlassian.net/jira/software/c/projects/JHP/issues/JHP-68
[JHP-69]: https://radiologics.atlassian.net/jira/software/c/projects/JHP/issues/JHP-69
