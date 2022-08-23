package org.nrg.xnatx.plugins.jupyterhub.repositories;

import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.xnatx.plugins.jupyterhub.entities.UserOptionsEntity;
import org.springframework.stereotype.Repository;

@Repository
public class UserOptionsDao extends AbstractHibernateDAO<UserOptionsEntity> {
}
