package net.iesmila.dam.yumeworld.server.api.messages.characterselection.out;

import net.iesmila.dam.yumeworld.server.api.messages.MessageBody;
import net.iesmila.dam.yumeworld.server.api.dto.CharacterInfo;
import net.iesmila.dam.yumeworld.server.api.dto.PlayerInfo;

import java.util.List;

public class LobbyUpdate_OUT extends MessageBody {

    public final static String TYPE = "LOBBY_UPDATE";

    public List<PlayerInfo> players;
    public List<CharacterInfo> characters;


    @Override
    public String getMessageType() {
        return TYPE;
    }


}