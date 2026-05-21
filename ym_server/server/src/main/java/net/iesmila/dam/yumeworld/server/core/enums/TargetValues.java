package net.iesmila.dam.yumeworld.server.core.enums;

public enum TargetValues {
    SELF('S'),
    TEAM('W'),
    ALLY('A'),
    ENEMY('E'),
    ALL_ENEMIES('T');

    public final char value;

    private TargetValues(char value) {
        this.value = value;
    }
}
