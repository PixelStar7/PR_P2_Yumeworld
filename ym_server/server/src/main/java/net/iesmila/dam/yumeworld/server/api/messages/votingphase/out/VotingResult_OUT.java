package net.iesmila.dam.yumeworld.server.api.messages.votingphase.out;

import net.iesmila.dam.yumeworld.server.api.messages.MessageBody;

public class VotingResult_OUT extends MessageBody {
    public final static String TYPE = "VOTING_RESULT";

    @Override
    public String getMessageType() {
        return TYPE;
    }
}
