package net.iesmila.dam.yumeworld.server.api.messages.rooms.in;

import net.iesmila.dam.yumeworld.server.api.messages.MessageBody;

/**
 * Missatge d'entrada que envia el client quan un jugador escull un objecte
 * concret del contingut d'un cofre (després de ChestOpened_OUT). El servidor
 * validarà la disponibilitat i, si és correcte, emetrà ItemPicked_OUT.
 */
public class PickItem_IN extends MessageBody {
    public final static String TYPE = "PICK_ITEM";

    public int playerId; // Jugador que vol agafar l'objecte
    public int itemId;   // Identificador de l'objecte (taula ACT) dins del cofre

    @Override
    public String getMessageType() {
        return TYPE;
    }
}
