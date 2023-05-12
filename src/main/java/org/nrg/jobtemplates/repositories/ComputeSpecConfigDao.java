package org.nrg.jobtemplates.repositories;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.jobtemplates.entities.ComputeSpecConfigEntity;
import org.nrg.jobtemplates.entities.ComputeSpecScopeEntity;
import org.nrg.jobtemplates.entities.HardwareConfigEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

@Repository
@Slf4j
public class ComputeSpecConfigDao extends AbstractHibernateDAO<ComputeSpecConfigEntity> {

    private final HardwareConfigDao hardwareConfigDao;

    // For testing
    public ComputeSpecConfigDao(final SessionFactory sessionFactory,
                                final HardwareConfigDao hardwareConfigDao) {
        super(sessionFactory);
        this.hardwareConfigDao = hardwareConfigDao;
    }

    @Autowired
    public ComputeSpecConfigDao(HardwareConfigDao hardwareConfigDao) {
        this.hardwareConfigDao = hardwareConfigDao;
    }

    /**
     * Initializes the entity, loading all collections and proxies.
     * @param entity The entity to initialize.
     */
    @Override
    public void initialize(final ComputeSpecConfigEntity entity) {
        if (entity == null) {
            return;
        }

        Hibernate.initialize(entity);

        Hibernate.initialize(entity.getConfigTypes());

        Hibernate.initialize(entity.getComputeSpec());
        if (entity.getComputeSpec() != null) {
            Hibernate.initialize(entity.getComputeSpec().getEnvironmentVariables());
            Hibernate.initialize(entity.getComputeSpec().getMounts());
        }

        Hibernate.initialize(entity.getScopes());
        if (entity.getScopes() != null) {
            entity.getScopes().forEach((scope, computeSpecScopeEntity) -> {
                initialize(computeSpecScopeEntity);
            });
        }

        Hibernate.initialize(entity.getHardwareOptions());
        if (entity.getHardwareOptions() != null) {
            Hibernate.initialize(entity.getHardwareOptions().getHardwareConfigs());

            if (entity.getHardwareOptions().getHardwareConfigs() != null) {
                for (HardwareConfigEntity hardwareConfig : entity.getHardwareOptions().getHardwareConfigs()) {
                    hardwareConfigDao.initialize(hardwareConfig);
                }
            }
        }
    }

    /**
     * Initializes the entity, loading all collections and proxies.
     * @param entity The entity to initialize.
     */
    public void initialize(ComputeSpecScopeEntity entity) {
        if (entity == null) {
            return;
        }

        Hibernate.initialize(entity);
        Hibernate.initialize(entity.getIds());
    }

    /**
     * Finds all compute spec configs that have the specified type.
     * @param type The type to search for.
     * @return The list of compute spec configs that have the specified type.
     */
    public List<ComputeSpecConfigEntity> findByType(String type) {
        // Need to use a criteria query because the configTypes field is a collection.
        Criteria criteria = getSession().createCriteria(ComputeSpecConfigEntity.class)
                .createCriteria("configTypes")
                .add(Restrictions.eq("elements", type));

        List<ComputeSpecConfigEntity> entities = criteria.list();

        if (entities == null) {
            return Collections.emptyList();
        }

        for (ComputeSpecConfigEntity entity : entities) {
            initialize(entity);
        }

        return entities;
    }

}
