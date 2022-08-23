package org.nrg.xnatx.plugins.jupyterhub.listeners;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xnat.event.EventListener;
import org.nrg.xnat.tracking.TrackEvent;
import org.nrg.xnatx.plugins.jupyterhub.events.JupyterServerEvent;
import reactor.bus.Event;

import reactor.fn.Consumer;

@Slf4j
@EventListener
public class JupyterServerEventListener implements Consumer<Event<JupyterServerEvent>> {
    @Override
    @TrackEvent
    public void accept(Event<JupyterServerEvent> busEvent) {
        log.debug("Received event {} for Jupyter notebook server event {}", busEvent.getId(), busEvent.getData());
    }
}
