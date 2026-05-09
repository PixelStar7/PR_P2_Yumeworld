package net.iesmila.dam.yumeworld.server.core.enums;

public enum RoomType {
    CHEST_EVENT("CHEST_INFO"),
    COMBAT("COMBAT_INFO"),
    REST_EVENT("REST_INFO"),
    BOSS_COMBAT("BOSS_INFO");

    public final String typeName;

    RoomType(String typeName) {
        this.typeName = typeName;
    }

    public String getTypeName() {
        return typeName;
    }
}
