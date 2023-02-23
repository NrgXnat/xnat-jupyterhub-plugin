package org.nrg.xnatx.plugins.jupyterhub.repositories;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.xnatx.plugins.jupyterhub.entities.ProfileEntity;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
public class ProfileDao extends AbstractHibernateDAO<ProfileEntity> {

}
