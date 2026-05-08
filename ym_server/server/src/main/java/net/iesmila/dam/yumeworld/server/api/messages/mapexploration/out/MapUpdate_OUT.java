package net.iesmila.dam.yumeworld.server.api.messages.mapexploration.out;

import net.iesmila.dam.yumeworld.server.api.messages.MessageBody;
import net.iesmila.dam.yumeworld.server.api.messages.mapexploration.minimodels.FloorInfo;

import java.util.List;

/**
 * Broadcast que envia el servidor amb l'estat del mapa de la partida.
 * S'utilitza tant per a l'enviament inicial (en entrar a StateMapVisualization)
 * com per a actualitzacions posteriors (canvi de sala actual, sala completada, etc.).
 */
public class MapUpdate_OUT extends MessageBody {
    public final static String TYPE = "MAP_UPDATE";

    public List<FloorInfo> floors;

    public MapUpdate_OUT() {}

    public MapUpdate_OUT(List<FloorInfo> floors) {
        this.floors = floors;
    }

    @Override
    public String getMessageType() {
        return TYPE;
    }
}
