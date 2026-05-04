package net.iesmila.dam.yumeworld.server.api.messages.combat.out;

import net.iesmila.dam.yumeworld.server.api.messages.MessageBody;

public class EndCombat_OUT extends MessageBody {
    public final static String TYPE = "COMBAT_END";

    @Override
    public String getMessageType() {
        return TYPE;
    }
}
