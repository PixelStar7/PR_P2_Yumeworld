package net.iesmila.dam.yumeworld.server.api.messages.combat.out;

import net.iesmila.dam.yumeworld.server.api.dto.EnemyInfo;
import net.iesmila.dam.yumeworld.server.api.dto.PlayerCombatSnapshot;
import net.iesmila.dam.yumeworld.server.api.messages.MessageBody;

import java.util.List;

/**
 * Broadcast d'actualització periòdica del combat. Conté l'estat agregat
 * de tots els participants (jugadors i enemics) i de qui és el torn ARA.
 *
 * S'envia després de cada acció (jugador o enemic), perquè el client pugui
 * refrescar la UI amb una sola lectura.
 */
public class CombatUpdate_OUT extends MessageBody {
    public final static String TYPE = "COMBAT_UPDATE";

    public List<PlayerCombatSnapshot> playerStates;
    public List<EnemyInfo> enemyStates;

    /**
     * ID del jugador que ha de fer el següent torn, o -1 si toca als enemics
     * (en aquest cas, els clients esperen un EnemyAction_OUT del servidor).
     */
    public long currentTurnPlayerId;

    public int round;  // round actual (comença a 1)

    public CombatUpdate_OUT() {}

    public CombatUpdate_OUT(List<PlayerCombatSnapshot> playerStates,
                            List<EnemyInfo> enemyStates,
                            long currentTurnPlayerId,
                            int round) {
        this.playerStates = playerStates;
        this.enemyStates = enemyStates;
        this.currentTurnPlayerId = currentTurnPlayerId;
        this.round = round;
    }

    @Override
    public String getMessageType() {
        return TYPE;
    }
}
