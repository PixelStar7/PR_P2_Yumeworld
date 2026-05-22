package net.iesmila.dam.yumeworld.server.core.enums;

public enum Codes {
    OK(0, "TOT OK"),
    INVALID_GAME_ID(1, "Error de validació de joc"),
    INVALID_PLAYER(2, "Error de validació de jugador"),
    INVALID_CHARACTER(3, "Error de validació de personatge"),
    CHARACTER_ALREADY_SELECTED(4, "El personatge ja està seleccionat"),
    INVALID_FLOOR(5, "Error de validació de pis"),
    INVALID_ROOM(6, "Error de validació de la room"),
    CHEST_NOT_OPEN(7, "Error. Intent d'agafar loot abans d'obrir el cofre."),
    CHEST_ALREADY_OPENED(8, "Error. El cofre ja està obert."),
    ITEM_ALREADY_TAKEN(9, "Error. L'ítem ja ha estat agafat."),
    INVALID_ITEM(10, "Error. Ítem no vàlid."),
    NOT_YOUR_TURN(11, "Error: No és el teu torn."),
    INVALID_ACTION(12, "Error: Acció no vàlida per a aquest personatge."),
    NOT_ENOUGH_MP(13, "Error: MP insuficient per executar aquesta acció."),
    CAMPFIRE_ALREADY_LIT(14, "Error: La foguera ja està encesa.");




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
