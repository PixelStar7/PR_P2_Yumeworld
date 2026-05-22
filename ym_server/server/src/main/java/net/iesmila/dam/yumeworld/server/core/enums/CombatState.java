package net.iesmila.dam.yumeworld.server.core.enums;

public enum CombatState {
    BATTLE_START,        // IniciBatalla
    PLAYER_TURN,         // TornJugador
    PROCESSING_ACTION,   // ProcessantAccio (Es calculen danys i efectes)
    CHECKING_STATE,      // ComprovantEstat (Condició de Victòria / Derrota / Enemics vius)
    ENEMY_TURN,          // TornEnemic
    PROCESSING_AI,       // ProcessantIA (Es calculen danys i efectes de l'enemic)
    BATTLE_END           // FinalBatalla
}



/**StateCharacterSelection
 → StateMapCreation
 → StateMapVisualization ←──────────┐
 ↓ (MoveToNode_IN)             │
 ┌────┼──────┬───────────┬──────────┤
 ↓    ↓      ↓           ↓          │
 StateCombat StateRest  StateChest    │
 ↓ victory     ↓          ↓          │
 ↓normal       ↓          ↓          │
 StateCombatRewards         ↓          │
 ↓             └──────────┴──────────┘
 StateMapVisualization
 ↓ (boss)
 StateCombat(isBoss=true)
 ↓ victory    ↓ defeat
 StateEndGame  StateEndGame
 (victory=true) (victory=false)
 */