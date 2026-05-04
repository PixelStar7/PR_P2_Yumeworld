package net.iesmila.dam.yumeworld.server.api.messages.rooms.in;

import net.iesmila.dam.yumeworld.server.api.messages.MessageBody;

public class LeaveRoom_IN extends MessageBody {
    public final static String TYPE = "LEAVE_ROOM";

    @Override
    public String getMessageType() {
        return TYPE;
    }
}
