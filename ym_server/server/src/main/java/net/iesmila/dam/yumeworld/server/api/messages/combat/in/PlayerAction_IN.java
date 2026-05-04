package net.iesmila.dam.yumeworld.server.api.messages.combat.in;

import net.iesmila.dam.yumeworld.server.api.messages.MessageBody;

public class PlayerAction_IN extends MessageBody {
    public final static String TYPE = "PlayerAction_IN";

    @Override
    public String getMessageType() {
        return TYPE;
    }
}
