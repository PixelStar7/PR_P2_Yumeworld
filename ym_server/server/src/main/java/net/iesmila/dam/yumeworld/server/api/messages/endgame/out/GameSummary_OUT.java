package net.iesmila.dam.yumeworld.server.api.messages.endgame.out;

import net.iesmila.dam.yumeworld.server.api.messages.MessageBody;

/**
 * Broadcast que envia el servidor en entrar a l'estat StateEndGame.
 * Conté un resum de la partida amb estadístiques globals i, eventualment,
 * desglossament per jugador. Els camps concrets es podran ampliar segons
 * les estadístiques que es vulguin mostrar al final.
 */
public class GameSummary_OUT extends MessageBody {
    public final static String TYPE = "GAME_SUMMARY";

    public boolean victory;       // Si el grup ha guanyat la partida
    public int floorsCleared;     // Nombre de pisos completats
    public int totalDamageDealt;  // Dany total fet pel grup
    public int totalCoins;        // Monedes acumulades

    // TODO: afegir desglossament per jugador (PlayerSummary minimodel) si cal.

    @Override
    public String getMessageType() {
        return TYPE;
    }
}
