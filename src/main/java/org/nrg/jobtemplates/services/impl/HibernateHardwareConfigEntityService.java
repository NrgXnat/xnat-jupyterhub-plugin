package org.nrg.jobtemplates.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.jobtemplates.entities.HardwareConfigEntity;
import org.nrg.jobtemplates.repositories.HardwareConfigDao;
import org.nrg.jobtemplates.services.HardwareConfigEntityService;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class HibernateHardwareConfigEntityService extends AbstractHibernateEntityService<HardwareConfigEntity, HardwareConfigDao> implements HardwareConfigEntityService {

}
