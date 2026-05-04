package net.iesmila.dam.yumeworld.server.api.messages.combat.in;

import net.iesmila.dam.yumeworld.server.api.messages.MessageBody;

public class combat_action extends MessageBody {
    public final static String TYPE = "COMBAT_ACTION";

    @Override
    public String getMessageType() {
        return TYPE;
    }
}
