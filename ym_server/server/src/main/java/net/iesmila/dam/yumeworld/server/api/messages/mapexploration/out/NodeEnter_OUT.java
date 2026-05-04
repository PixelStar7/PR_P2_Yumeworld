package net.iesmila.dam.yumeworld.server.api.messages.mapexploration.out;

import net.iesmila.dam.yumeworld.server.api.messages.MessageBody;

public class NodeEnter_OUT extends MessageBody {
    public final static String TYPE = "NODE_ENTER";

    @Override
    public String getMessageType() {
        return TYPE;
    }
}
