package net.iesmila.dam.yumeworld.server.api.messages.votingphase.in;

import net.iesmila.dam.yumeworld.server.api.messages.MessageBody;

public class SubmitVote_IN extends MessageBody {
    public final static String TYPE = "SUBMIT_VOTE";

    @Override
    public String getMessageType() {
        return TYPE;
    }
}
