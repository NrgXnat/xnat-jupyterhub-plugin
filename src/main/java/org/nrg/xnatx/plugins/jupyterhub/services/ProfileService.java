package org.nrg.xnatx.plugins.jupyterhub.services;

import org.nrg.xapi.exceptions.DataFormatException;
import org.nrg.xnatx.plugins.jupyterhub.models.Profile;

import java.util.List;
import java.util.Optional;

public interface ProfileService {

    boolean exists(Long id);
    Long create(Profile profile) throws DataFormatException;
    Optional<Profile> get(Long id);
    List<Profile> getAll();
    void update(Profile profile) throws DataFormatException;
    void delete(Long id);

}