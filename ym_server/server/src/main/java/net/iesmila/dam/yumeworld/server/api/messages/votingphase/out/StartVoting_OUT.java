package net.iesmila.dam.yumeworld.server.api.messages.votingphase.out;

import net.iesmila.dam.yumeworld.server.api.messages.MessageBody;

import java.util.List;

/**
 * Broadcast que envia el servidor en entrar a l'estat StateVotingPhase.
 * Inicialitza la votació al client mostrant les opcions disponibles
 * (per exemple, les rooms candidates a explorar).
 */
public class StartVoting_OUT extends MessageBody {
    public final static String TYPE = "START_VOTING";

    public List<Integer> optionIds; // Identificadors de les opcions sobre les quals es vota

    @Override
    public String getMessageType() {
        return TYPE;
    }
}
