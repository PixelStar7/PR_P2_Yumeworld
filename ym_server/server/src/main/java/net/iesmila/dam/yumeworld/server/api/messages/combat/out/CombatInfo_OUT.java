package net.iesmila.dam.yumeworld.server.api.messages.combat.out;

import net.iesmila.dam.yumeworld.server.api.dto.EnemyInfo;
import net.iesmila.dam.yumeworld.server.api.messages.MessageBody;

import java.util.List;

/**
 * Broadcast que envia el servidor en entrar a StateCombat. Conté tota la
 * informació inicial del combat: enemics presents, ordre de torns dels
 * jugadors (decidit aleatòriament al constructor) i qui té el primer torn.
 */
public class CombatInfo_OUT extends MessageBody {
    public final static String TYPE = "COMBAT_INFO";

    public boolean isBoss;
    public List<EnemyInfo> enemies;
    public List<Long> playerTurnOrder;   // ids dels jugadors, en l'ordre que actuaran
    public long currentTurnPlayerId;     // qui té el primer torn (-1 si toca als enemics, no hauria de ser el primer)

    public CombatInfo_OUT() {}

    public CombatInfo_OUT(boolean isBoss, List<EnemyInfo> enemies,
                          List<Long> playerTurnOrder, long currentTurnPlayerId) {
        this.isBoss = isBoss;
        this.enemies = enemies;
        this.playerTurnOrder = playerTurnOrder;
        this.currentTurnPlayerId = currentTurnPlayerId;
    }

    @Override
    public String getMessageType() {
        return TYPE;
    }
}
