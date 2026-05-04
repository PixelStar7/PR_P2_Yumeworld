package net.iesmila.dam.yumeworld.server.api.messages.mapexploration.minimodels;

public class RoomInfo {
    public int id;
    public String type;
    public boolean is_cleared;
    public boolean is_current;

    public RoomInfo() {}

    public RoomInfo(int id, String type, boolean is_cleared, boolean is_current) {
        this.id = id;
        this.type = type;
        this.is_cleared = is_cleared;
        this.is_current = is_current;
    }

}
