package net.iesmila.dam.yumeworld.server.api.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

public class JSONMessage {
    public String messageType;
    public String gameId;
    public JsonNode data;

    @JsonCreator
    public JSONMessage(){}

    /**
     * Constructor per enviar missatges donat un objecte de dades que hem preparat
     * @GameId id del joc
     * @param messageBodyObject les dades que s'envien del missatge
     */
    public JSONMessage(String gameId, MessageBody messageBodyObject) {
        ObjectMapper mapper = new ObjectMapper();
        this.messageType = messageBodyObject.getMessageType();
        this.gameId = gameId;
        this.data = mapper.valueToTree(messageBodyObject);
    }
}