package net.iesmila.dam.yumeworld.server.core.enums;

public enum RoomTypes {
    CHEST_EVENT("CHEST_INFO"),
    COMBAT("COMBAT_INFO"),
    REST_EVENT("REST_INFO"),
    BOSS_COMBAT("BOSS_INFO");

    public final String typeName;

    RoomTypes(String typeName) {
        this.typeName = typeName;
    }

    public String getTypeName() {
        return typeName;
    }
}
