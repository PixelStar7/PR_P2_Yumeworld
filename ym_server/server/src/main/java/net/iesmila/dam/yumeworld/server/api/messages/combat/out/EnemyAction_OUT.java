package net.iesmila.dam.yumeworld.server.api.messages.combat.out;

import net.iesmila.dam.yumeworld.server.api.messages.MessageBody;

/**
 * Missatge de sortida que envia el servidor quan un enemic executa una acció
 * dins del combat. Permet diferenciar visualment l'acció de l'enemic respecte
 * a una actualització genèrica del combat (CombatUpdate_OUT).
 */
public class EnemyAction_OUT extends MessageBody {
    public final static String TYPE = "ENEMY_ACTION";

    public int enemyId;          // Identificador de l'enemic que actua
    public int actionId;         // Identificador de l'acció executada (taula ACT)
    public int targetPlayerId;   // Jugador objectiu de l'acció (si escau)

    @Override
    public String getMessageType() {
        return TYPE;
    }
}
