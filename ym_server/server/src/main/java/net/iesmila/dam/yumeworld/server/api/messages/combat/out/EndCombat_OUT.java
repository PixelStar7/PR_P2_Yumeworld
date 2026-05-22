package net.iesmila.dam.yumeworld.server.api.messages.combat.out;

import net.iesmila.dam.yumeworld.server.api.messages.MessageBody;

/**
 * Broadcast que envia el servidor en acabar un combat amb victòria.
 * Aquesta versió és un FLAG sense dades — només indica al client que
 * el combat s'ha acabat i que pot canviar d'escena.
 *
 * Les recompenses concretes (XP, items) arriben després via
 * CombatRewards_OUT des de StateCombatRewards.
 *
 * Per a la derrota es fa servir GameOver_OUT.
 */
public class EndCombat_OUT extends MessageBody {
    public final static String TYPE = "END_COMBAT";

    @Override
    public String getMessageType() {
        return TYPE;
    }
}
