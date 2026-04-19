package net.iesmila.dam.yumeworld.server.api.messages;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

public abstract class MessageBody {

    /*
    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            include = JsonTypeInfo.As.PROPERTY,
            property = "type"
    )
    @JsonSubTypes({
            // Registrem les classes filles i el seu codi associat.
            // Quan Jackson vegi "type": "LOBBY_UPDATE", instanciarà LobbyUpdate_OUT directament!
            @JsonSubTypes.Type(value = net.iesmila.dam.yumeworld.server.api.messages.characterselection.out.LobbyUpdate_OUT.class, name = "LOBBY_UPDATE"),
            @JsonSubTypes.Type(value = net.iesmila.dam.yumeworld.server.api.messages.characterselection.in.PickCharacterMessage_IN.class, name = "PICK_CHARACTER")

    })
    */


    public long timestamp;

    public MessageBody() {
        this.timestamp = System.currentTimeMillis();
    }

    @JsonIgnore
    public abstract String getMessageType();
}
