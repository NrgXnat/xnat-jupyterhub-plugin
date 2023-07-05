# XNAT JupyterHub Plugin

[XNAT](https://www.xnat.org) plugin for integrating with [JupyterHub](https://jupyter.org/hub). You will need to have a
running instance of the [xnat/jupyterhub](https://hub.docker.com/r/xnat/jupyterhub) Docker image to use this plugin 
([source](https://bitbucket.org/xnat-containers/xnat-jupyterhub)). See the 
[XNAT Jupyter Integration Wiki](https://wiki.xnat.org/jupyter-integration) for the latest documentation.

The plugin jar is available on the [downloads page](https://bitbucket.org/xnatx/xnat-jupyterhub-plugin/downloads/) 
and on [XNAT's CI server](https://ci.xnat.org/job/Plugins_Release/job/JupyterHub).

## Building the JAR

To build the JAR, run the following command from the root of the project:

```bash
./gradlew clean jar
```

The JAR will be built in the `build/libs` directory.

## Running the tests

To run the tests, run the following command from the root of the project:

```bash
./gradlew clean test
```
