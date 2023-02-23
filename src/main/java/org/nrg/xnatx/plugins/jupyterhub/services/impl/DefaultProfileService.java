package org.nrg.xnatx.plugins.jupyterhub.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xapi.exceptions.DataFormatException;
import org.nrg.xnatx.plugins.jupyterhub.entities.ProfileEntity;
import org.nrg.xnatx.plugins.jupyterhub.models.Profile;
import org.nrg.xnatx.plugins.jupyterhub.services.ProfileEntityService;
import org.nrg.xnatx.plugins.jupyterhub.services.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Default implementation of the profile service. Valid profiles must have a name, description, task template, container
 * spec, and image defined.
 */
@Service
@Slf4j
public class DefaultProfileService implements ProfileService {

    private final ProfileEntityService profileEntityService;

    @Autowired
    public DefaultProfileService(final ProfileEntityService profileEntityService) {
        this.profileEntityService = profileEntityService;
    }

    /**
     * Check if a profile exists.
     * @param id profile id
     * @return true if the profile exists, false otherwise
     */
    @Override
    public boolean exists(final Long id) {
        final ProfileEntity profileEntity = profileEntityService.retrieve(id);
        return profileEntity != null;
    }

    /**
     * Create a profile.
     * @param profile profile to create
     * @return id of the created profile
     *
     * @throws DataFormatException if the profile is invalid
     */
    @Override
    public Long create(final Profile profile) throws DataFormatException {
        profile.setId(null);

        // Validate the profile
        validate(profile);

        final ProfileEntity profileEntity = ProfileEntity.fromPojo(profile);
        ProfileEntity created = profileEntityService.create(profileEntity);
        created.getProfile().setId(created.getId()); // Update the id in the profile
        profileEntityService.update(created);
        return created.getId();
    }

    /**
     * Get a profile.
     * @param id profile id
     * @return optional profile if it exists
     */
    @Override
    public Optional<Profile> get(final Long id) {
        Optional<ProfileEntity> profileEntity = Optional.ofNullable(profileEntityService.retrieve(id));
        return profileEntity.map(ProfileEntity::toPojo);
    }

    /**
     * Get all profiles.
     * @return list of profiles
     */
    @Override
    public List<Profile> getAll() {
        return profileEntityService.getAll()
                .stream()
                .map(ProfileEntity::toPojo)
                .collect(Collectors.toList());
    }

    /**
     * Update an existing profile.
     * @param toUpdate profile to update
     *
     * @throws DataFormatException if the profile is invalid
     */
    @Override
    public void update(Profile toUpdate) throws DataFormatException {
        final ProfileEntity profileEntity = profileEntityService.retrieve(toUpdate.getId());

        // Profile not found
        if (profileEntity == null) {
            DataFormatException exception = new DataFormatException();
            exception.addInvalidField("id", "Profile not found");
            throw exception;
        }

        // Validate the profile
        validate(toUpdate);

        final ProfileEntity template = ProfileEntity.fromPojo(toUpdate);
        final ProfileEntity updated = profileEntity.update(template);

        profileEntityService.update(updated);
    }

    /**
     * Delete a profile.
     * @param id profile id
     */
    @Override
    public void delete(final Long id) {
        profileEntityService.delete(id);
    }

    /**
     * Validate a profile. Profile is invalid if:
     * - name is empty
     * - description is empty
     * - task template is empty
     * - container spec is empty
     * - image is empty
     *
     * @param profile profile to validate
     * @throws DataFormatException if the profile is invalid
     */
    protected void validate(final Profile profile) throws DataFormatException {
        DataFormatException exception = new DataFormatException();

        if (profile.getName() == null || profile.getName().isEmpty()) {
            exception.addInvalidField('"' + "name" + '"' + " cannot be empty");
        }

        if (profile.getDescription() == null || profile.getDescription().isEmpty()) {
            exception.addInvalidField('"' + "description" + '"' + " cannot be empty");
        }

        if (profile.getTaskTemplate() == null) {
            exception.addInvalidField('"' + "taskTemplate" + '"' + " cannot be empty");
            throw exception;
        }

        if (profile.getTaskTemplate().getContainerSpec() == null) {
            exception.addInvalidField('"' + "containerSpec" + '"' + " cannot be empty");
            throw exception;
        }

        if (profile.getTaskTemplate().getContainerSpec().getImage() == null || profile.getTaskTemplate().getContainerSpec().getImage().isEmpty()) {
            exception.addInvalidField('"' + "image" + '"' + " cannot be empty");
        }

        if (exception.hasDataFormatErrors()) {
            throw exception;
        }
    }

}
