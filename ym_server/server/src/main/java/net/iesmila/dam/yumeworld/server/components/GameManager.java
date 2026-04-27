package net.iesmila.dam.yumeworld.server.components;

import net.iesmila.dam.yumeworld.server.api.models.Player;
import net.iesmila.dam.yumeworld.server.db.repository.RepositoryProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * El gestor de joc es responsabilitza de gestionar les noves connexions WS i crear les partides
 * quan hi ha prous jugadors. Un cop establert el WS amb cada jugador, també
 * s'encarrega de recepcionar els missatges dels WS ja connectats, redirigint-los a les
 * partides pertinents.
 */
@Component
public class GameManager {

    private long lastPlayerId = 0;

    // Per a obtenir tots els repositoris de dades de la BD
    @Autowired
    private RepositoryProvider repoProvider;

    /** Executor de fils */
    private ExecutorService executor;

    /** Cua de jugadors pendents d'assignar a partida */
    private final Queue<WebSocketSession> waitingPlayers = new ConcurrentLinkedQueue<>();


    /** mapa que relaciona Ids de sessió de websocket(String) amb partides actuals (GameInstance).
     * Un websocket session id pertany a un únic player connectat.*/
    private final Map<WebSocketSession, GameInstance> sessionToGame = new ConcurrentHashMap<>();

    private final Map<WebSocketSession, Player> sessionToPlayer = new ConcurrentHashMap<>();

    /** Índex de GameInstance per game_id */
    private final Map<String, GameInstance> games = new ConcurrentHashMap<>();
    private int numPlayersPerGame = 2;


    public GameManager(){
        executor = Executors.newFixedThreadPool(10);
        System.out.println("Server started on port 8080! :P");
    }

    public void onConnect(WebSocketSession session) {
        waitingPlayers.add(session);
        tryStartGame();
    }

    public void handleIncoming(WebSocketSession session, String message) {
        // Mirem si hi ha un joc associat a aquesta sessió, i si és el cas
        // encuem la petició al joc trobat.
        GameInstance game = sessionToGame.get(session);
        if (game != null) {
            Player p = sessionToPlayer.get(session);
            game.enqueue(new GameMessage(p, message));
        }
    }

    private void tryStartGame() {
        System.out.println("Number of waiting players:"+waitingPlayers.size()   );
        if (waitingPlayers.size() >= numPlayersPerGame) {
            List<Player> players = new ArrayList<>();
            for (int i = 0; i < numPlayersPerGame; i++) {
                    players.add(new Player( lastPlayerId++, waitingPlayers.poll()));
            }

            GameInstance game = new GameInstance(players, executor, repoProvider);
            String gameId = game.getId();

            // Registrem els jugadors al mapa de sessions-->joc i sessions->Player
            players.forEach(p -> sessionToGame.put(p.getSession(), game));
            players.forEach(p -> sessionToPlayer.put(p.getSession(), p));

            // Indexem el joc pel seu Id
            games.put(gameId, game);

            // Engeguem el fil de joc
            game.start();
        }
    }
}
