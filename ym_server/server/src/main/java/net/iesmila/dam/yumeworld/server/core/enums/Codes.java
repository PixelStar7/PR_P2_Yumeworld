package net.iesmila.dam.yumeworld.server.core.enums;

public enum Codes {
    OK(0, "TOT OK"),
    INVALID_GAME_ID(1, "Error de validació de joc"),
    INVALID_PLAYER(2, "Error de validació de jugador"),
    INVALID_CHARACTER(3, "Error de validació de personatge"),
    CHARACTER_ALREADY_SELECTED(4, "El personatge ja està seleccionat");



    public final int value;
    public final String desc;

    private Codes(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    // Mètode per obtenir un Code a partir del seu valor numèric
    public static Codes fromValue(int value) {
        for (Codes code : Codes.values()) {
            if (code.value == value) {
                return code;
            }
        }
        // Si no trobem el codi, podem retornar un valor per defecte o llançar una excepció
        // Per simplicitat, retornarem OK si no es troba, però es podria millorar
        return OK; // O llançar IllegalArgumentException("Codi d'error desconegut: " + value);
    }
}
