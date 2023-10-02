package org.nrg.xnatx.plugins.jupyterhub.utils;

public interface SystemHelper {

    default String getEnv(String name) {
        return System.getenv(name);
    }

}
