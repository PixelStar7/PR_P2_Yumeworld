package net.iesmila.dam.yumeworld.server.api.messages.combat.out;

import net.iesmila.dam.yumeworld.server.api.messages.MessageBody;

public record combat_start() implements MessageBody {
    public final static String TYPE = "COMBAT_START";

    @Override
    public String getMessageType() {
        return TYPE;
    }
}
