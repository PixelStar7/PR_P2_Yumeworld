package net.iesmila.dam.yumeworld.server.api.messages.mapexploration.minimodels;

import java.util.List;

public class FloorInfo {
    public int num;
    public String name;
    public String biomeName;
    public List<RoomInfo> rooms;

    public FloorInfo() {}

    public FloorInfo(int num, String name, String biomeName, List<RoomInfo> rooms) {
        this.num = num;
        this.name = name;
        this.biomeName = biomeName;
        this.rooms = rooms;
    }
}
