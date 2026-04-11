package net.iesmila.dam.yumeworld.server.components.enums;

public enum CombatState {
    BATTLE_START,        // IniciBatalla
    PLAYER_TURN,         // TornJugador
    PROCESSING_ACTION,   // ProcessantAccio (Es calculen danys i efectes)
    CHECKING_STATE,      // ComprovantEstat (Condició de Victòria / Derrota / Enemics vius)
    ENEMY_TURN,          // TornEnemic
    PROCESSING_AI,       // ProcessantIA (Es calculen danys i efectes de l'enemic)
    BATTLE_END           // FinalBatalla
}
