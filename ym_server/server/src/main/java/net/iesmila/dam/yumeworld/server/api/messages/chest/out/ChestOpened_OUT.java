package net.iesmila.dam.yumeworld.server.api.messages.chest.out;

import net.iesmila.dam.yumeworld.server.api.dto.LootInfo;
import net.iesmila.dam.yumeworld.server.api.messages.MessageBody;

import java.util.List;

/**
 * Broadcast que envia el servidor quan un cofre s'ha obert correctament.
 * Notifica a tots els jugadors el contingut del cofre (LootInfo amb tota la
 * info necessària: name, description, imageUrl, effects...) perquè la UI
 * pugui mostrar les opcions de selecció (PickItem_IN / ItemPicked_OUT).
 */
public class ChestOpened_OUT extends MessageBody {
    public final static String TYPE = "CHEST_OPENED";

    public long openedByPlayerId;        // Jugador que ha obert el cofre
    public List<LootInfo> availableItems; // Objectes disponibles per agafar

    public ChestOpened_OUT() {}

    public ChestOpened_OUT(long openedByPlayerId, List<LootInfo> availableItems) {
        this.openedByPlayerId = openedByPlayerId;
        this.availableItems = availableItems;
    }

    @Override
    public String getMessageType() {
        return TYPE;
    }
}
