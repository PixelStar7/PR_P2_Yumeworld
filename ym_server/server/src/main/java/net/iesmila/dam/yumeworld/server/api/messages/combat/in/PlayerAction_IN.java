package net.iesmila.dam.yumeworld.server.api.messages.combat.in;

import net.iesmila.dam.yumeworld.server.api.messages.MessageBody;

/**
 * Missatge d'entrada que envia el client quan és el torn d'un jugador
 * i tria una acció.
 *
 * Camps:
 *   - playerId: qui actua (long).
 *   - actionId: ID de l'acció a la taula ACT.
 *   - targetCombatId: identificador d'instància de l'objectiu dins del combat:
 *       · si targetType="ENEMY" → combatId de l'EnemyCombatState.
 *       · si targetType="PLAYER" → playerId del jugador objectiu (cast a int).
 *       · si targetType="SELF"/"TEAM"/"ALL_ENEMIES" → s'ignora.
 *   - targetType: una de: ENEMY, PLAYER, SELF, TEAM, ALL_ENEMIES.
 */
public class PlayerAction_IN extends MessageBody {
    public final static String TYPE = "PLAYER_ACTION";

    public long playerId;
    public int actionId;
    public int targetCombatId;   // s'ignora per a SELF/TEAM/ALL_ENEMIES
    public String targetType;    // "ENEMY" | "PLAYER" | "SELF" | "TEAM" | "ALL_ENEMIES"

    @Override
    public String getMessageType() {
        return TYPE;
    }
}
