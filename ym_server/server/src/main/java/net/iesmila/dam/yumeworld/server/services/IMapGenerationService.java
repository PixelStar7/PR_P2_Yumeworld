package net.iesmila.dam.yumeworld.server.services;

import net.iesmila.dam.yumeworld.server.api.dto.FloorInfo;
import net.iesmila.dam.yumeworld.server.core.enums.RoomType;

public interface IMapGenerationService {

    /**
     * Genera un pis del mapa. Un pis és una única fila de nodes; el jugador
     * només en visitarà un per pis. El bioma del pis es calcula a partir de
     * la seva posició dins del total de pisos: el primer pis tindrà el bioma
     * més suau i l'últim el més intens (nightmare).
     */
    FloorInfo generateFloor(int floorNum, int totalFloors, int numColumns);

    /**
     * Genera un pis on totes les sales són del tipus indicat. Igual que
     * generateFloor, el bioma es calcula a partir de la posició del pis.
     */
    FloorInfo generateTypeFloor(int floorNum, int totalFloors, int numColumns, RoomType type);
}
