package net.iesmila.dam.yumeworld.server.api.dto;

import java.util.List;

public class FloorInfo {
    public int num;
    public String name;
    /** ID del bioma a la BD; el fa servir el servidor per buscar els enemics
     *  d'aquest pis a ENEMY_BIOMES. */
    public int biomeId;
    public String biomeName;
    /** URL del background del bioma (camp bi_background); el client el pinta. */
    public String biomeBackground;
    public List<RoomInfo> rooms;

    public FloorInfo() {}

    public FloorInfo(int num, String name, int biomeId, String biomeName,
                     String biomeBackground, List<RoomInfo> rooms) {
        this.num = num;
        this.name = name;
        this.biomeId = biomeId;
        this.biomeName = biomeName;
        this.biomeBackground = biomeBackground;
        this.rooms = rooms;
    }
}
