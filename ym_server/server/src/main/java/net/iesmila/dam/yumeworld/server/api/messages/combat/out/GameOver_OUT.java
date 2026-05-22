package net.iesmila.dam.yumeworld.server.api.messages.combat.out;

import net.iesmila.dam.yumeworld.server.api.messages.MessageBody;

/**
 * Broadcast que envia el servidor quan tots els jugadors moren en combat.
 * Després d'aquest missatge, transició a StateEndGame amb victory=false.
 */
public class GameOver_OUT extends MessageBody {
    public final static String TYPE = "GAME_OVER";

    @Override
    public String getMessageType() {
        return TYPE;
    }
}
