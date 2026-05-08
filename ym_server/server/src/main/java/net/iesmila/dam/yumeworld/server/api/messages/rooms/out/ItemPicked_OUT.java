package net.iesmila.dam.yumeworld.server.api.messages.rooms.out;

import net.iesmila.dam.yumeworld.server.api.messages.MessageBody;

/**
 * Broadcast que envia el servidor quan un jugador ha agafat correctament
 * un objecte d'un cofre. Permet a tots els clients actualitzar la llista
 * d'objectes disponibles i veure qui s'ha quedat què.
 */
public class ItemPicked_OUT extends MessageBody {
    public final static String TYPE = "ITEM_PICKED";

    public int playerId; // Jugador que ha agafat l'objecte
    public int itemId;   // Identificador de l'objecte (taula ACT) agafat

    @Override
    public String getMessageType() {
        return TYPE;
    }
}
