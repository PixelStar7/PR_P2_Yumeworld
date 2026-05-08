package net.iesmila.dam.yumeworld.server.api.messages.combatrewards.in;

import net.iesmila.dam.yumeworld.server.api.messages.MessageBody;

/**
 * Missatge d'entrada que envia el client quan el jugador confirma que ha vist
 * o recollit la seva recompensa de combat. Quan tots els jugadors hagin enviat
 * aquest missatge, el servidor emetrà AllRewardsClaimed_OUT i farà la transició.
 */
public class ClaimReward_IN extends MessageBody {
    public final static String TYPE = "CLAIM_REWARD";

    public int playerId; // Jugador que confirma haver recollit la recompensa

    @Override
    public String getMessageType() {
        return TYPE;
    }
}
