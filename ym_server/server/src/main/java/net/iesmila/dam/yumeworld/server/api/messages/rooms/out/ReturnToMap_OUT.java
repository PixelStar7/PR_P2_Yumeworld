package net.iesmila.dam.yumeworld.server.api.messages.rooms.out;

import net.iesmila.dam.yumeworld.server.api.messages.MessageBody;

/**
 * Broadcast que envia el servidor just abans de transicionar d'una sala
 * (rest, chest, etc.) cap a StateMapVisualization. Indica al client que ha
 * de mostrar la vista del mapa de nou.
 *
 * Acostuma a anar seguit d'un MapUpdate_OUT amb l'estat actualitzat del mapa
 * (sala visitada marcada com a is_cleared, etc.).
 */
public class ReturnToMap_OUT extends MessageBody {
    public final static String TYPE = "RETURN_TO_MAP";

    @Override
    public String getMessageType() {
        return TYPE;
    }
}
