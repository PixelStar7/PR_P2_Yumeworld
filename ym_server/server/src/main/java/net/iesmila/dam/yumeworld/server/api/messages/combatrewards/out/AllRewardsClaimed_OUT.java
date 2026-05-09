package net.iesmila.dam.yumeworld.server.api.messages.combatrewards.out;

import net.iesmila.dam.yumeworld.server.api.messages.MessageBody;

/**
 * Broadcast que envia el servidor quan tots els jugadors han confirmat la seva
 * recompensa amb ClaimReward_IN. Indica que es procedeix a la transició cap al
 * següent estat (típicament StateMapVisualization).
 */
public class AllRewardsClaimed_OUT extends MessageBody {
    public final static String TYPE = "ALL_REWARDS_CLAIMED";

    @Override
    public String getMessageType() {
        return TYPE;
    }
}
