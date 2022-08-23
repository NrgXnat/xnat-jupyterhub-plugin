package org.nrg.xnatx.plugins.jupyterhub.services;

import org.nrg.framework.orm.hibernate.BaseHibernateService;
import org.nrg.xnatx.plugins.jupyterhub.entities.UserOptionsEntity;

import java.util.Optional;

public interface UserOptionsEntityService extends BaseHibernateService<UserOptionsEntity> {

    void createOrUpdate(UserOptionsEntity userOptionsEntity);
    Optional<UserOptionsEntity> find(Integer userId, String servername);

}
