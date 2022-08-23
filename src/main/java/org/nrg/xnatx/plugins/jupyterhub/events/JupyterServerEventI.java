package org.nrg.xnatx.plugins.jupyterhub.events;

import org.nrg.framework.status.StatusMessage;
import org.nrg.xnat.tracking.model.TrackableEvent;

public interface JupyterServerEventI extends TrackableEvent {

    enum Operation {
        Start,
        Stop
    }

    enum Status {
        InProgress,
        Warning,
        Completed,
        Failed;

        public StatusMessage.Status status() {
            switch (this) {
                case Completed:
                    return StatusMessage.Status.COMPLETED;

                case Failed:
                    return StatusMessage.Status.FAILED;

                case InProgress:
                    return StatusMessage.Status.PROCESSING;

                default:
                    return StatusMessage.Status.WARNING;
            }
        }
    }

    String getXsiType();
    String getItemId();
    Status getStatus();
    Operation getOperation();
    int getProgress();
    long getEventTime();

}
