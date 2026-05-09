package net.iesmila.dam.yumeworld.server.api.messages.characterselection.in;

import net.iesmila.dam.yumeworld.server.api.messages.MessageBody;

public class PickCharacter_IN extends MessageBody {

    public final static String TYPE = "PICK_CHARACTER";

    public long playerId;
    public int characterId;

    @Override
    public String getMessageType() {
        return TYPE;
    }
}


