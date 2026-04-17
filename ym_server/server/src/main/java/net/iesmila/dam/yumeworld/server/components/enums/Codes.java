package net.iesmila.dam.yumeworld.server.components.enums;

public enum Codes {
    OK(0, "TOT OK"),
    UNVALID_PLAYER(1, "Error de validació de jugador"),
    UNVALID_CHARACTER(2, "Error de validació de personatge");



    public final int value;
    public final String desc;

    private Codes(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
