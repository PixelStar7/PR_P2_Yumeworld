package net.iesmila.dam.yumeworld.server.api.messages.combat.out;

import net.iesmila.dam.yumeworld.server.api.dto.CombatRewardsResult;
import net.iesmila.dam.yumeworld.server.api.messages.MessageBody;

/**
 * Broadcast que envia el servidor en acabar un combat amb victòria.
 * Conté les recompenses obtingudes (XP, loot).
 *
 * Per a la derrota, es fa servir GameOver_OUT.
 */
public class EndCombat_OUT extends MessageBody {
    public final static String TYPE = "END_COMBAT";

    public CombatRewardsResult rewards;

    public EndCombat_OUT() {}

    public EndCombat_OUT(CombatRewardsResult rewards) {
        this.rewards = rewards;
    }

    @Override
    public String getMessageType() {
        return TYPE;
    }
}
