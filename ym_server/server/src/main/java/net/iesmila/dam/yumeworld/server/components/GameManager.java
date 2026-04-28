package net.iesmila.dam.yumeworld.server.components;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.iesmila.dam.yumeworld.server.api.messages.JSONMessage;
import net.iesmila.dam.yumeworld.server.api.messages.generic.ActionResult_OUT;
import net.iesmila.dam.yumeworld.server.api.messages.generic.Login_OUT;
import net.iesmila.dam.yumeworld.server.api.models.Player;
import net.iesmila.dam.yumeworld.server.components.enums.Codes;
import net.iesmila.dam.yumeworld.server.db.repository.RepositoryProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
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

    private final ObjectMapper objectMapper = new ObjectMapper();


    public GameManager(){
        executor = Executors.newFixedThreadPool(10);
        System.out.println("Server started on port 8080! :P");
    }

    public void onConnect(WebSocketSession session) {
        System.out.println("[GameManager] Nova connexió establerta: " + session.getId());
        waitingPlayers.add(session);
        tryStartGame();
    }

    public void handleIncoming(WebSocketSession session, String message) {
        System.out.println("[GameManager] Rebut missatge de " + session.getId() + ": " + message);

        // 1. Intentar parsejar el missatge per obtenir el gameId
        String gameId = null;
        try {
            JsonNode json = objectMapper.readTree(message);
            if (json.has("gameId")) {
                gameId = json.get("gameId").asText();
            }
        } catch (IOException e) {
            System.err.println("[GameManager] Error parsejant el missatge JSON de " + session.getId() + ": " + e.getMessage());
            sendError(session, Codes.INVALID_GAME_ID.value, "Format de missatge JSON invàlid o sense 'gameId'.");
            return;
        }

        // 2. Validar que el gameId existeix al missatge
        if (gameId == null || gameId.isEmpty()) {
            System.err.println("[GameManager] Missatge de " + session.getId() + " sense 'gameId'.");
            sendError(session, Codes.INVALID_GAME_ID.value, "El missatge ha d'incloure un 'gameId'.");
            return;
        }

        // 3. Obtenir la GameInstance associada a la sessió
        GameInstance game = sessionToGame.get(session);

        // 4. Validar que la sessió està associada a una partida
        if (game == null) {
            System.err.println("[GameManager] Sessió " + session.getId() + " no associada a cap partida.");
            sendError(session, Codes.INVALID_GAME_ID.value, "La teva sessió no està associada a cap partida activa.");
            return;
        }

        // 5. Validar que el gameId del missatge coincideix amb el de la partida de la sessió
        if (!game.getId().equals(gameId)) {
            System.err.println("[GameManager] GameId del missatge (" + gameId + ") no coincideix amb el de la sessió (" + game.getId() + ") per a " + session.getId());
            sendError(session, Codes.INVALID_GAME_ID.value, "L'ID de partida del missatge no coincideix amb la teva partida actual.");
            return;
        }

        // Si totes les validacions són correctes, encuem el missatge a la partida
        Player p = sessionToPlayer.get(session);
        if (p != null) {
            game.enqueue(new GameMessage(p, message));
            System.out.println("[GameManager] Missatge de " + session.getId() + " encolat correctament a la partida " + game.getId());
        } else {
            System.err.println("[GameManager] Error: Jugador no trobat per a la sessió " + session.getId() + " tot i estar en una partida.");
            sendError(session, Codes.INVALID_PLAYER.value, "No s'ha pogut identificar el teu jugador a la partida.");
        }
    }

    private void sendError(WebSocketSession session, int errorCode, String errorMessage) {
        try {
            ActionResult_OUT errorResult = new ActionResult_OUT(false, errorCode, errorMessage);
            String jsonError = objectMapper.writeValueAsString(new JSONMessage("SERVER_ERROR", errorResult));
            session.sendMessage(new TextMessage(jsonError));
        } catch (IOException e) {
            System.err.println("[GameManager] Error enviant missatge d'error a " + session.getId() + ": " + e.getMessage());
        }
    }

    public void onDisconnect(WebSocketSession session) {
        System.out.println("[GameManager] Connexió tancada: " + session.getId());
        waitingPlayers.remove(session); // Si estava en espera, el treiem

        GameInstance game = sessionToGame.remove(session); // Treiem la sessió del mapa de partides
        sessionToPlayer.remove(session); // Treiem el jugador del mapa

        if (game != null) {
            System.out.println("[GameManager] Jugador de la sessió " + session.getId() + " desconnectat de la partida " + game.getId());
            // Aquí podríem afegir lògica per gestionar la desconnexió dins de la GameInstance
            // per exemple, marcar el jugador com a inactiu o finalitzar la partida si és crítica.
        }
    }


    private void tryStartGame() {
        System.out.println("[GameManager] Intentant iniciar partida. Jugadors en espera: "+waitingPlayers.size()   );
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
            System.out.println("[GameManager] Partida " + gameId + " iniciada amb " + numPlayersPerGame + " jugadors.");




        }
    }
}
