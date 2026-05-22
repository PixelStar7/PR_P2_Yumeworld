package net.iesmila.dam.yumeworld.server.api.messages.combat.out;

import net.iesmila.dam.yumeworld.server.api.messages.MessageBody;

/**
 * Missatge de sortida que envia el servidor quan un enemic executa una acció
 * dins del combat. Permet diferenciar visualment l'acció de l'enemic respecte
 * a una actualització genèrica del combat (CombatUpdate_OUT).
 */
public class EnemyAction_OUT extends MessageBody {
    public final static String TYPE = "ENEMY_ACTION";

    public int enemyCombatId;    // identificador d'instància de l'enemic dins del combat
    public int enemyId;          // tipus de l'enemic (FK BD)
    public int actionId;         // identificador de l'acció executada (taula ACT)
    public long targetPlayerId;  // jugador objectiu (-1 si l'acció no té objectiu únic)

    public EnemyAction_OUT() {}

    public EnemyAction_OUT(int enemyCombatId, int enemyId, int actionId, long targetPlayerId) {
        this.enemyCombatId = enemyCombatId;
        this.enemyId = enemyId;
        this.actionId = actionId;
        this.targetPlayerId = targetPlayerId;
    }

    @Override
    public String getMessageType() {
        return TYPE;
    }
}
