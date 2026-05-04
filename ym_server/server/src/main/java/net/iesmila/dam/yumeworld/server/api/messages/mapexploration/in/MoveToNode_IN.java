package net.iesmila.dam.yumeworld.server.api.messages.mapexploration.in;

import net.iesmila.dam.yumeworld.server.api.messages.MessageBody;

public class MoveToNode_IN extends MessageBody {
    public final static String TYPE = "MOVE_TO_NODE";

    @Override
    public String getMessageType() {
        return TYPE;
    }
}
