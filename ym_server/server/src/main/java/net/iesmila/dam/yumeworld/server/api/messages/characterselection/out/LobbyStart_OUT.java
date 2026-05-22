package net.iesmila.dam.yumeworld.server.api.messages.characterselection.out;

import net.iesmila.dam.yumeworld.server.api.messages.MessageBody;

public class LobbyStart_OUT extends MessageBody {

    public final static String TYPE = "LOBBY_START";
    public int numPlayers;


    public LobbyStart_OUT() {}


    public LobbyStart_OUT(int size) {
        this.numPlayers = size;
    }


    @Override
    public String getMessageType() {
        return TYPE;
    }

}
