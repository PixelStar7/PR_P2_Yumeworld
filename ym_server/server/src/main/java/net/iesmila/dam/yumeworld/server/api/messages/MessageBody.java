package net.iesmila.dam.yumeworld.server.api.messages;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class MessageBody {

    @JsonIgnore
    public abstract String getMessageType();
}
