package net.iesmila.dam.yumeworld.server.api.messages.generic.out;

import net.iesmila.dam.yumeworld.server.api.messages.MessageBody;

import java.util.List;

/**
 * Broadcast que envia el servidor després de processar un ReadyToLeave_IN.
 * Conté la llista actualitzada de jugadors que ja han confirmat sortir,
 * perquè els clients puguin mostrar quins companys encara queden per
 * confirmar (UI tipus "esperant la Marta...").
 *
 * Quan tots els jugadors són a la llista, el servidor també envia
 * ReturnToMap_OUT i fa la transició a StateMapVisualization.
 */
public class PlayersReadyToLeave_OUT extends MessageBody {
    public final static String TYPE = "PLAYERS_READY_TO_LEAVE";

    public List<Long> readyPlayerIds; // Tots els jugadors que han enviat ReadyToLeave_IN fins ara

    public PlayersReadyToLeave_OUT() {}

    public PlayersReadyToLeave_OUT(List<Long> readyPlayerIds) {
        this.readyPlayerIds = readyPlayerIds;
    }

    @Override
    public String getMessageType() {
        return TYPE;
    }
}
