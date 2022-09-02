package org.nrg.xnatx.plugins.jupyterhub.utils.impl;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xft.schema.XFTManager;
import org.nrg.xnatx.plugins.jupyterhub.utils.XFTManagerHelper;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DefaultXFTManagerHelper implements XFTManagerHelper {

    @Override
    public boolean isInitialized() {
        return XFTManager.isInitialized();
    }

}
