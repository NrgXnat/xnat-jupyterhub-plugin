package org.nrg.xnatx.plugins.jupyterhub.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.nrg.xdat.XDAT;
import org.nrg.xnatx.plugins.jupyterhub.models.JupyterServerEventTrackingLog;

import java.io.IOException;


@Data
@Builder
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
@Slf4j
public class JupyterServerEvent implements JupyterServerEventI {

    public static JupyterServerEvent completed(final String trackingId, final Integer userId, final Operation operation, final String message) {
        return builder().trackingId(trackingId).userId(userId).xsiType(null).itemId(null).operation(operation).status(Status.Completed).progress(100).eventTime(System.currentTimeMillis()).message(message).build();
    }

    public static JupyterServerEvent completed(final String trackingId, final Integer userId, final String xsiType, final String itemId, final Operation operation, final String message) {
        return builder().trackingId(trackingId).userId(userId).xsiType(xsiType).itemId(itemId).operation(operation).status(Status.Completed).progress(100).eventTime(System.currentTimeMillis()).message(message).build();
    }

    public static JupyterServerEvent failed(final String trackingId, final Integer userId, final Operation operation, final String message) {
        return builder().trackingId(trackingId).userId(userId).xsiType(null).itemId(null).operation(operation).status(Status.Failed).progress(100).eventTime(System.currentTimeMillis()).message(message).build();
    }

    public static JupyterServerEvent failed(final String trackingId, final Integer userId, final String xsiType, final String itemId, final Operation operation, final String message) {
        return builder().trackingId(trackingId).userId(userId).xsiType(xsiType).itemId(itemId).operation(operation).status(Status.Failed).progress(100).eventTime(System.currentTimeMillis()).message(message).build();
    }

    public static JupyterServerEvent progress(final String trackingId, final Integer userId, final Operation operation, final int progress, final String message) {
        return builder().trackingId(trackingId).userId(userId).xsiType(null).itemId(null).operation(operation).status(Status.InProgress).progress(progress).eventTime(System.currentTimeMillis()).message(message).build();
    }

    public static JupyterServerEvent progress(final String trackingId, final Integer userId, final String xsiType, final String itemId, final Operation operation, final int progress, final String message) {
        return builder().trackingId(trackingId).userId(userId).xsiType(xsiType).itemId(itemId).operation(operation).status(Status.InProgress).progress(progress).eventTime(System.currentTimeMillis()).message(message).build();
    }

    private final String trackingId;
    private final Integer userId;
    private final String xsiType;
    private final String itemId;
    private final Operation operation;
    private final Status status;
    private final int progress;
    private final long eventTime;
    private final String message;

    @Override
    public boolean isSuccess() {
        return status != Status.Failed;
    }

    @Override
    public boolean isCompleted() {
        return progress == 100;
    }

    @Override
    public String updateTrackingPayload(@Nullable String currentPayload) throws IOException {
        JupyterServerEventTrackingLog statusLog;
        if (currentPayload != null) {
            statusLog = XDAT.getSerializerService()
                            .getObjectMapper()
                            .readValue(currentPayload, JupyterServerEventTrackingLog.class);
        } else {
            statusLog = new JupyterServerEventTrackingLog();
        }
        statusLog.addToEntryList(new JupyterServerEventTrackingLog.MessageEntry(status, eventTime, message));
        statusLog.sortEntryList();
        return XDAT.getSerializerService().getObjectMapper().writeValueAsString(statusLog);
    }
}
