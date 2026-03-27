package net.iesmila.dam.yumeworld.server.components;

public enum GameState {
    CHARACTER_SELECTION, // Selecció de personatges
    MAP_EXPLORATION,     // Visualitzar mapa
    TREASURE_ROOM,       // Cofre
    REST_ROOM,           // Descans
    COMBAT,              // Combat normal
    BOSS_COMBAT,         // Combat Boss Final
    END_GAME             // Pantalla de Victòria / Derrota
}
