package org.nrg.xnatx.plugins.jupyterhub.services.impl;


import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.xnatx.plugins.jupyterhub.entities.ProfileEntity;
import org.nrg.xnatx.plugins.jupyterhub.repositories.ProfileDao;
import org.nrg.xnatx.plugins.jupyterhub.services.ProfileEntityService;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@Slf4j
@Transactional
public class HibernateProfileEntityService extends AbstractHibernateEntityService<ProfileEntity, ProfileDao> implements ProfileEntityService {

}
