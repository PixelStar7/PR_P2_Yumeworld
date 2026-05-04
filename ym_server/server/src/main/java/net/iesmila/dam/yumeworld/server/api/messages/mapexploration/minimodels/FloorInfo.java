package net.iesmila.dam.yumeworld.server.api.messages.mapexploration.minimodels;

import java.util.List;

public class FloorInfo {
    int num;
    String name;
    String biome_path;
    List<RoomInfo> rooms;

    FloorInfo(){}

    public FloorInfo(int num, String name, String biome_path, List<RoomInfo> rooms) {
        this.num = num;
        this.name = name;
        this.biome_path = biome_path;
        this.rooms = rooms;
    }

}
