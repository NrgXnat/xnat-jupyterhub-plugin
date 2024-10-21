package org.nrg.xnatx.plugins.jupyterhub.services.impl;

import lombok.extern.slf4j.Slf4j;
import javax.validation.constraints.NotNull;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.xnatx.plugins.jupyterhub.entities.UserOptionsEntity;
import org.nrg.xnatx.plugins.jupyterhub.repositories.UserOptionsDao;
import org.nrg.xnatx.plugins.jupyterhub.services.UserOptionsEntityService;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class HibernateUserOptionsEntityService extends AbstractHibernateEntityService<UserOptionsEntity, UserOptionsDao> implements UserOptionsEntityService {

    /**
     * Creates or updates the provided user options
     * @param userOptionsEntity the user options entity to create or update
     */
    @Override
    @Transactional
    public void createOrUpdate(@NotNull UserOptionsEntity userOptionsEntity) {
        Integer userId = userOptionsEntity.getUserId();
        String servername = userOptionsEntity.getServername();

        Optional<UserOptionsEntity> found = find(userId, servername);

        if (found.isPresent()) {
            log.debug("Updating user options entity for user id '{}' servername '{}'",
                      userId, servername);

            found.get().update(userOptionsEntity);
            update(found.get());
        } else {
            log.debug("Creating user options entity for user id '{}' servername '{}'",
                      userId, servername);

            create(userOptionsEntity);
        }
    }

    /**
     * Find user options for the provided user and server.
     * @param userId the user id
     * @param servername can be an empty string
     * @return found user options or empty
     */
    @Override
    @Transactional
    public Optional<UserOptionsEntity> find(Integer userId, String servername) {
        log.debug("Finding user options entity for user id '{}' servername '{}'",
                  userId, servername);

        Map<String, Object> properties = new HashMap<>();
        properties.put("userId", userId);
        properties.put("servername", servername);
        List<UserOptionsEntity> serverConfigEntities = getDao().findByProperties(properties);

        if (serverConfigEntities == null || serverConfigEntities.isEmpty()) {
            return Optional.empty();
        } else if (serverConfigEntities.size() > 1) {
            log.error("The specified userId={} and servername={} is not a unique constraint!", userId, servername);
            throw new RuntimeException("The specified userId and servername is not a unique constraint!");
        }

        return Optional.of(serverConfigEntities.get(0));
    }
}
