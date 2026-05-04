package net.iesmila.dam.yumeworld.server.api.messages.votingphase.out;

import net.iesmila.dam.yumeworld.server.api.messages.MessageBody;

public class VoteUpdate_OUT extends MessageBody {
    public final static String TYPE = "VOTE_UPDATE";

    @Override
    public String getMessageType() {
        return TYPE;
    }
}
