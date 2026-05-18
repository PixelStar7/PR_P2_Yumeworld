package net.iesmila.dam.yumeworld.server.api.messages.combat.out;

import net.iesmila.dam.yumeworld.server.api.messages.MessageBody;

/**
 * Broadcast d'una aplicació de dany a un objectiu concret durant el combat.
 *
 * - actorId: qui fa el dany. Si actorType="PLAYER", és el playerId.
 *            Si actorType="ENEMY", és el combatId de l'EnemyCombatState.
 * - targetId: qui rep el dany. Mateix conveni segons targetType.
 * - damageAmount: quant ha rebut.
 * - newHp: HP restant del target després del dany (per al client).
 */
public class TakeDamage_OUT extends MessageBody {
    public final static String TYPE = "TAKE_DAMAGE";

    public long actorId;
    public String actorType;    // "PLAYER" | "ENEMY"
    public long targetId;
    public String targetType;   // "PLAYER" | "ENEMY"
    public int damageAmount;
    public int newHp;

    public TakeDamage_OUT() {}

    public TakeDamage_OUT(long actorId, String actorType,
                          long targetId, String targetType,
                          int damageAmount, int newHp) {
        this.actorId = actorId;
        this.actorType = actorType;
        this.targetId = targetId;
        this.targetType = targetType;
        this.damageAmount = damageAmount;
        this.newHp = newHp;
    }

    @Override
    public String getMessageType() {
        return TYPE;
    }
}
