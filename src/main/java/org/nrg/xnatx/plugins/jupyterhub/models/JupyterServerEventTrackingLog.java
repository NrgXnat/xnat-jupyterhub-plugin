package org.nrg.xnatx.plugins.jupyterhub.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import javax.validation.constraints.NotNull;
import org.nrg.xnatx.plugins.jupyterhub.events.JupyterServerEventI;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Slf4j
public class JupyterServerEventTrackingLog {

    @Builder.Default private List<MessageEntry> entryList = new ArrayList<>();

    public void addToEntryList(MessageEntry entry) {
        this.entryList.add(entry);
    }

    public void sortEntryList(){
        Collections.sort(this.entryList);
    }

    @JsonInclude
    public static class MessageEntry implements Comparable<MessageEntry> {
        private JupyterServerEventI.Status status;
        private long eventTime;
        @Nullable
        private String message;

        public MessageEntry() {}

        public MessageEntry(JupyterServerEventI.Status status, long eventTime, @Nullable String message) {
            this.status = status;
            this.eventTime = eventTime;
            this.message = message;
        }

        public JupyterServerEventI.Status getStatus() {
            return status;
        }

        public void setStatus(JupyterServerEventI.Status status) {
            this.status = status;
        }

        @Nullable
        public String getMessage() {
            return message;
        }

        public void setMessage(@Nullable String message) {
            this.message = message;
        }

        public long getEventTime() {
            return eventTime;
        }

        public void setEventTime(long eventTime) {
            this.eventTime = eventTime;
        }

        @Override
        public int compareTo(@NotNull MessageEntry o) {
            return Long.compare(this.eventTime, o.eventTime);
        }
    }
}
