package net.iesmila.dam.yumeworld.server.api.messages.endgame.out;

import net.iesmila.dam.yumeworld.server.api.dto.PlayerSummary;
import net.iesmila.dam.yumeworld.server.api.messages.MessageBody;

import java.util.List;

/**
 * Broadcast que envia el servidor en entrar a l'estat StateEndGame.
 * Conté un resum de la partida: si s'ha guanyat o no, quants pisos s'han
 * completat i les estadístiques finals de cada jugador.
 *
 * NOTA: 'totalCoins' s'ha eliminat perquè no hi ha sistema de monedes
 * implementat (vegeu en_given_coins a la BD, pendent per a botiga futura).
 * Quan hi hagi botiga, es pot afegir aquí.
 *
 * NOTA: 'totalDamageDealt' s'ha eliminat de moment perquè no acumulem
 * estadístiques durant la partida. Per afegir-ho caldria un GameStats al
 * GameInstance que es vagi actualitzant a cada acció.
 */
public class GameSummary_OUT extends MessageBody {
    public final static String TYPE = "GAME_SUMMARY";

    public boolean victory;                       // Si el grup ha guanyat la partida
    public int floorsCleared;                     // Nombre de pisos completats
    public List<PlayerSummary> playerSummaries;   // Estadístiques finals per jugador

    public GameSummary_OUT() {}

    public GameSummary_OUT(boolean victory, int floorsCleared, List<PlayerSummary> playerSummaries) {
        this.victory = victory;
        this.floorsCleared = floorsCleared;
        this.playerSummaries = playerSummaries;
    }

    @Override
    public String getMessageType() {
        return TYPE;
    }
}
