package net.iesmila.dam.yumeworld.server.api.messages.mapexploration.out;

import net.iesmila.dam.yumeworld.server.api.messages.MessageBody;

public class MapUpdate_OUT extends MessageBody {
    public final static String TYPE = "MAP_UPDATE";

    @Override
    public String getMessageType() {
        return TYPE;
    }
}
