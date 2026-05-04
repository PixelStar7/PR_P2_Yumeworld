package net.iesmila.dam.yumeworld.server.api.messages.combat.out;

import net.iesmila.dam.yumeworld.server.api.messages.MessageBody;

public record turn_update() implements MessageBody {
    public final static String TYPE = "TURN_UPDATE";

    @Override
    public String getMessageType() {
        return TYPE;
    }
}
