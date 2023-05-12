package org.nrg.jobtemplates.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.jobtemplates.entities.ConstraintConfigEntity;
import org.nrg.jobtemplates.repositories.ConstraintConfigDao;
import org.nrg.jobtemplates.services.ConstraintConfigEntityService;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class HibernateConstraintConfigEntityService extends AbstractHibernateEntityService<ConstraintConfigEntity, ConstraintConfigDao> implements ConstraintConfigEntityService {

}
