package net.iesmila.dam.yumeworld.server.api.messages.rooms.out;

import net.iesmila.dam.yumeworld.server.api.messages.MessageBody;

import java.util.List;

/**
 * Broadcast que envia el servidor quan un cofre s'ha obert correctament.
 * Notifica a tots els jugadors el contingut del cofre perquè la UI pugui
 * mostrar les opcions de selecció (PickItem_IN / ItemPicked_OUT).
 */
public class ChestOpened_OUT extends MessageBody {
    public final static String TYPE = "CHEST_OPENED";

    public int openedByPlayerId;       // Jugador que ha obert el cofre
    public List<Integer> availableItemIds; // IDs dels objectes (taula ACT) disponibles per agafar

    @Override
    public String getMessageType() {
        return TYPE;
    }
}
