package net.iesmila.dam.yumeworld.server.api.messages.combat.out;

import net.iesmila.dam.yumeworld.server.api.dto.ActionInfo;
import net.iesmila.dam.yumeworld.server.api.messages.MessageBody;

import java.util.List;

/**
 * Missatge individual (no broadcast) enviat a cada jugador al inici del combat,
 * just després de {@code COMBAT_INFO}. Conté la llista completa d'accions que
 * el jugador pot usar durant el combat: accions pròpies del personatge
 * (YUMEDANT_ACT) i habilitats heretades del seu rol (PLAYERROLE_ABILITY).
 *
 * El client usa aquest missatge per renderitzar el menú d'accions del torn
 * i mostrar el cost MP de cadascuna. L'identificador {@code actionId} de cada
 * acció és el que el client ha d'enviar de tornada al servidor en {@code PLAYER_ACTION}.
 *
 * Enviament: individual per sessió (game.send), no broadcast.
 * Moment: just després del COMBAT_INFO, un cop inicialitzats els torns.
 */
public class PlayerActions_OUT extends MessageBody {
    public static final String TYPE = "PLAYER_ACTIONS";

    /** Llista d'accions disponibles per al jugador receptor d'aquest missatge. */
    public List<ActionInfo> actions;

    public PlayerActions_OUT() {}

    public PlayerActions_OUT(List<ActionInfo> actions) {
        this.actions = actions;
    }

    @Override
    public String getMessageType() {
        return TYPE;
    }
}
