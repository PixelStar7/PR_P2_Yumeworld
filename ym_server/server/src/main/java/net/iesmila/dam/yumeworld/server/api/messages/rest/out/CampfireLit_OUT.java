package net.iesmila.dam.yumeworld.server.api.messages.rest.out;

import net.iesmila.dam.yumeworld.server.api.messages.MessageBody;

/**
 * Broadcast que envia el servidor quan la foguera ja s'ha encès i la curació
 * s'ha aplicat. Després d'aquest missatge, els clients poden mostrar el botó
 * de "sortir" perquè cada jugador enviï el seu ReadyToLeave_IN al seu ritme.
 *
 * Els canvis concrets d'stats dels personatges arriben per missatges separats
 * de tipus CharacterUpdate_OUT (un per jugador afectat).
 */
public class CampfireLit_OUT extends MessageBody {
    public final static String TYPE = "CAMPFIRE_LIT";

    public long ignitedByPlayerId; // Jugador que ha encès la foguera

    public CampfireLit_OUT() {}

    public CampfireLit_OUT(long ignitedByPlayerId) {
        this.ignitedByPlayerId = ignitedByPlayerId;
    }

    @Override
    public String getMessageType() {
        return TYPE;
    }
}
