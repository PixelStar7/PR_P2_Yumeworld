package net.iesmila.dam.yumeworld.server.api.dto;

/**
 * DTO d'una sala dins d'un pis.
 *
 * NOTA: "on és el grup ARA mateix" NO viu aquí — viu al GameInstance amb
 * currentFloor + currentRoomId. El MapUpdate_OUT els porta perquè el client
 * els tingui de cop. Aquest DTO només té estat persistent de la sala:
 * el seu tipus i si ja s'ha completat (is_cleared).
 */
public class RoomInfo {
    public int id;
    public String type;
    public boolean is_cleared;

    public RoomInfo() {}

    public RoomInfo(int id, String type, boolean is_cleared) {
        this.id = id;
        this.type = type;
        this.is_cleared = is_cleared;
    }
}
