package net.iesmila.dam.yumeworld.server.api.messages;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

public abstract class MessageBody {



    public long timestamp;

    public MessageBody() {
        this.timestamp = System.currentTimeMillis();
    }

    @JsonIgnore
    public abstract String getMessageType();
}
