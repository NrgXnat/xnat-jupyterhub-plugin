package org.nrg.xnatx.plugins.jupyterhub.services.impl;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.nrg.xapi.exceptions.DataFormatException;
import org.nrg.xnatx.plugins.jupyterhub.config.DefaultProfileServiceConfig;
import org.nrg.xnatx.plugins.jupyterhub.entities.ProfileEntity;
import org.nrg.xnatx.plugins.jupyterhub.models.Profile;
import org.nrg.xnatx.plugins.jupyterhub.models.docker.ContainerSpec;
import org.nrg.xnatx.plugins.jupyterhub.models.docker.Placement;
import org.nrg.xnatx.plugins.jupyterhub.models.docker.Resources;
import org.nrg.xnatx.plugins.jupyterhub.models.docker.TaskTemplate;
import org.nrg.xnatx.plugins.jupyterhub.services.ProfileEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = DefaultProfileServiceConfig.class)
public class DefaultProfileServiceTest {

    @Autowired private DefaultProfileService defaultProfileService;
    @Autowired private ProfileEntityService mockProfileEntityService;

    @After
    public void tearDown() {
        Mockito.reset(mockProfileEntityService);
    }

    @Test
    public void exists() {
        // create a profileEntity with an id
        final Long profileId = 1L;
        final ProfileEntity profileEntity = new ProfileEntity();
        profileEntity.setId(profileId);

        // mock the profileEntityService.retrieve() method to return a profileEntity
        Mockito.when(mockProfileEntityService.retrieve(profileId)).thenReturn(profileEntity);

        // call the method under test
        final boolean profileExists = defaultProfileService.exists(profileId);

        // verify profileEntityService.retrieve() is called
        Mockito.verify(mockProfileEntityService, Mockito.times(1)).retrieve(profileId);

        // verify the method under test returns true
        assert(profileExists);
    }

    @Test
    public void doesNotExist() {
        // set the profile id
        final Long profileId = 1L;

        // mock the profileEntityService.retrieve() method to return null
        Mockito.when(mockProfileEntityService.retrieve(anyLong())).thenReturn(null);

        // call the method under test
        final boolean profileExists = defaultProfileService.exists(profileId);

        // verify profileEntityService.retrieve() is called
        Mockito.verify(mockProfileEntityService, Mockito.times(1)).retrieve(profileId);

        // verify the method under test returns false
        assert(!profileExists);
    }

    @Test
    public void create() throws Exception {
        // Setup profile
        final Profile profile = createProfile(null, "profile1", "description1", "image1");

        final ProfileEntity profileEntity = new ProfileEntity();
        profileEntity.setId(1L);
        profileEntity.setProfile(profile);

        // mock the profileEntityService.create() method to return a profileEntity
        Mockito.when(mockProfileEntityService.create(any(ProfileEntity.class))).thenReturn(profileEntity);

        // call the method under test
        final Long profileId = defaultProfileService.create(profile);

        // verify profileEntityService.create() is called
        Mockito.verify(mockProfileEntityService, Mockito.times(1)).create(profileEntity);

        // verify the method under test returns the correct profile id
        assert(profileId.equals(profileEntity.getId()));

        // verify the profile id is set
        assert(profile.getId().equals(profileEntity.getId()));
    }

    @Test(expected = DataFormatException.class)
    public void create_invalid() throws Exception {
        // Setup profile
        final Profile profile = createProfile(null, null, null, null);

        // call the method under test
        defaultProfileService.create(profile);
    }

    @Test
    public void get() {
        // Setup profile
        final Profile profile = createProfile(1L, "profile1", "description1", "image1");

        final ProfileEntity profileEntity = new ProfileEntity();
        profileEntity.setId(profile.getId());
        profileEntity.setProfile(profile);

        // mock the profileEntityService.retrieve() method to return a profileEntity
        Mockito.when(mockProfileEntityService.retrieve(profile.getId())).thenReturn(profileEntity);

        // call the method under test
        final Optional<Profile> profileOptional = defaultProfileService.get(profile.getId());

        // verify profileEntityService.retrieve() is called
        Mockito.verify(mockProfileEntityService, Mockito.times(1)).retrieve(profile.getId());

        // verify the method under test returns the correct profile
        assert(profileOptional.isPresent());
        assert(profileOptional.get().getId().equals(profile.getId()));
        assert(profileOptional.get().getName().equals(profile.getName()));
    }

    @Test
    public void get_doesNotExist() {
        // set the profile id
        final Long profileId = 1L;

        // mock the profileEntityService.retrieve() method to return null
        Mockito.when(mockProfileEntityService.retrieve(anyLong())).thenReturn(null);

        // call the method under test
        final Optional<Profile> profileOptional = defaultProfileService.get(profileId);

        // verify profileEntityService.retrieve() is called
        Mockito.verify(mockProfileEntityService, Mockito.times(1)).retrieve(profileId);

        // verify the method under test returns an empty optional
        assert(!profileOptional.isPresent());
    }

    @Test
    public void getAll() {
        // Setup profiles
        final Profile profile1 = createProfile(1L, "profile1", "description1", "image1");
        final Profile profile2 = createProfile(2L, "profile2", "description2", "image2");

        final ProfileEntity profileEntity1 = new ProfileEntity();
        profileEntity1.setId(profile1.getId());
        profileEntity1.setProfile(profile1);

        final ProfileEntity profileEntity2 = new ProfileEntity();
        profileEntity2.setId(profile2.getId());
        profileEntity2.setProfile(profile2);

        final List<ProfileEntity> profileEntities = Arrays.asList(profileEntity1, profileEntity2);

        // mock the profileEntityService.getAll() method to return a list of profileEntities
        Mockito.when(mockProfileEntityService.getAll()).thenReturn(profileEntities);

        // call the method under test
        final List<Profile> profiles = defaultProfileService.getAll();

        // verify profileEntityService.getAll() is called
        Mockito.verify(mockProfileEntityService, Mockito.times(1)).getAll();

        // verify the method under test returns the correct list of profiles
        assert(profiles.size() == 2);
        assert(profiles.get(0).getId().equals(profile1.getId()));
        assert(profiles.get(0).getName().equals(profile1.getName()));
        assert(profiles.get(1).getId().equals(profile2.getId()));
        assert(profiles.get(1).getName().equals(profile2.getName()));
    }

    @Test
    public void update() throws DataFormatException {
        // Setup profile
        final Long profileId = 1L;

        final Profile oldProfile = createProfile(profileId, "oldName", "oldDescription", "oldImage");
        final Profile updatedProfile = createProfile(profileId, "updatedName", "updatedDescription", "updatedImage");

        // Setup profileEntity
        final ProfileEntity profileEntity = new ProfileEntity();
        profileEntity.setId(profileId);
        profileEntity.setProfile(oldProfile);

        // mock the profileEntityService.retrieve() method to return a profileEntity
        Mockito.when(mockProfileEntityService.retrieve(profileId)).thenReturn(profileEntity);

        // call the method under test
        defaultProfileService.update(updatedProfile);

        // verify profileEntityService.retrieve() is called
        Mockito.verify(mockProfileEntityService, Mockito.times(1)).retrieve(profileId);

        // verify profileEntityService.update() is called
        Mockito.verify(mockProfileEntityService, Mockito.times(1)).update(profileEntity);

        // verify the profileEntity has the updated profile
        assert(profileEntity.getProfile().getId().equals(updatedProfile.getId()));
        assert(profileEntity.getProfile().getName().equals(updatedProfile.getName()));
    }

    @Test(expected = DataFormatException.class)
    public void update_invalid() throws Exception {
        // Setup profile
        final Long profileId = 1L;

        final Profile updatedProfile = createProfile(profileId, null, null, null);

        // call the method under test
        defaultProfileService.update(updatedProfile);
    }

    @Test(expected = DataFormatException.class)
    public void update_doesNotExist() throws Exception {
        // Setup profile
        final Long profileId = 1L;

        final Profile updatedProfile = createProfile(profileId, "updatedName", "updatedDescription", "updatedImage");

        // call the method under test
        defaultProfileService.update(updatedProfile);
    }

    @Test
    public void delete() {
        // set profile id
        final Long profileId = 1L;

        // call the method under test
        defaultProfileService.delete(profileId);

        // verify profileEntityService.delete() is called
        Mockito.verify(mockProfileEntityService, Mockito.times(1)).delete(profileId);
    }

    private Profile createProfile(Long id, String name, String description, String image) {
        ContainerSpec containerSpec = ContainerSpec.builder()
                .image(image)
                .mounts(Collections.emptyList())
                .env(Collections.emptyMap())
                .labels(Collections.emptyMap())
                .build();

        Placement placement = Placement.builder()
                .constraints(Collections.emptyList())
                .build();

        Resources resources = Resources.builder()
                .cpuLimit(null)
                .cpuReservation(null)
                .memLimit(null)
                .memReservation(null)
                .genericResources(Collections.emptyMap())
                .build();

        TaskTemplate taskTemplate = TaskTemplate.builder()
                .containerSpec(containerSpec)
                .placement(placement)
                .resources(resources)
                .build();

        return Profile.builder()
                .id(id)
                .name(name)
                .description(description)
                .taskTemplate(taskTemplate)
                .build();
    }

}