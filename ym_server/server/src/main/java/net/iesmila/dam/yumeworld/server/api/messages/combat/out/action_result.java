package net.iesmila.dam.yumeworld.server.api.messages.combat.out;

import net.iesmila.dam.yumeworld.server.api.messages.MessageBody;

public class action_result extends MessageBody {
    public final static String TYPE = "ACTION_RESULT";

    @Override
    public String getMessageType() {
        return TYPE;
    }
}
