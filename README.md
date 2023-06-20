# XNAT JupyterHub Plugin

[XNAT](https://www.xnat.org) plugin for integrating with [JupyterHub](https://jupyter.org/hub).

See the [XNAT JupyterHub Plugin Wiki](https://wiki.xnat.org/jupyter-integration) for the latest documentation on this 
plugin.

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
