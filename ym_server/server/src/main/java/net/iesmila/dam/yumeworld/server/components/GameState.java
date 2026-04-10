package net.iesmila.dam.yumeworld.server.components;

public enum GameState {
    CHARACTER_SELECTION, // Selecció de personatges
    MAP_EXPLORATION,     // Visualitzar mapa
    VOTING_PHASE,
    CHEST_EVENT,       // Cofre
    REST_EVENT,           // Descans
    COMBAT,              // Combat normal
    BOSS_COMBAT,         // Combat Boss Final
    COMBAT_REWARDS,
    END_GAME             // Pantalla de Victòria / Derrota
}
