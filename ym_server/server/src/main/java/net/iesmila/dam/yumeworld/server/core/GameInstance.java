package net.iesmila.dam.yumeworld.server.core;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import net.iesmila.dam.yumeworld.server.api.messages.JSONMessage;
import net.iesmila.dam.yumeworld.server.api.messages.generic.out.Login_OUT;
import net.iesmila.dam.yumeworld.server.api.messages.mapexploration.minimodels.FloorInfo;
import net.iesmila.dam.yumeworld.server.api.models.Player;
import net.iesmila.dam.yumeworld.server.api.states.State;
import net.iesmila.dam.yumeworld.server.api.states.StateCharacterSelection;
import net.iesmila.dam.yumeworld.server.db.repository.RepositoryProvider;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.TextMessage;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;


import java.io.IOException;

import java.util.concurrent.atomic.AtomicBoolean;

import java.util.UUID;

/**
 * Classe que representa una partida i tot el seu estat:
 *  - jugadors connectats
 *  - cua de missatges
 *  - estat intern del joc
 */
public class GameInstance {

    /**
     * Els jugadors de la partida
     */
    private final List<Player> players;

    /**
     * Llista de missatges per gestionar-ho (INBOX)
     */
    private final BlockingQueue<GameMessage> queue = new LinkedBlockingQueue<>();

    /**
     * Executor de fils
     */
    private final ExecutorService executor;

    /**
     * Mutex per controlar que el joc només tingui 1 fil en execució
     */
    private final AtomicBoolean threadIsRunning = new AtomicBoolean(false);

    /**
     * Indica si la partida està en curs (true) o ha acabat (false)
     */
    private volatile boolean gameActive = true;

    /**
     * Id de la partida (UUID)
     */
    private final String id;


    // Variables d'informació de la partida

    /**
     * Mapa generat un sol cop a StateMapCreation.
     * Persisteix durant tota la partida i és consultat per StateMapVisualization
     * cada vegada que s'hi torna entre rooms.
     */
    private FloorInfo[] floors;

    /**
     * Posició actual del grup dins del mapa.
     * currentFloor: índex del pis on es troba el grup (-1 si encara no n'ha visitat cap).
     * currentRoomId: id de la sala dins d'aquell pis (-1 si encara no està definit).
     * Tots dos camps els actualitza StateMapVisualization quan algú confirma un MoveToNode_IN.
     */
    private volatile int currentFloor = -1;
    private volatile int currentRoomId = -1;

    // Variables que controlen en quin punt del diagrama estem
    private volatile State currentState;

    // Map per associar jugadors amb els seus personatges
    // private HashMap<Integer, Integer> userCharacterMap = new HashMap<>();

    protected ObjectMapper mapper = new ObjectMapper();

    // Bossa de Repositoris de la BD
    private final RepositoryProvider repoProvider;


    public GameInstance(List<Player> players, ExecutorService executor, RepositoryProvider repoProvider) {
        this.repoProvider = repoProvider;
        this.id = UUID.randomUUID().toString();
        this.players = players;
        this.executor = executor;
        
        // Log dels jugadors en iniciar la instància
        String playerIds = players.stream()
                                 .map(p -> String.valueOf(p.getId()))
                                 .collect(Collectors.joining(", "));
        System.out.println("Starting the game instance: " + this.id + " with players: [" + playerIds + "]");

        // Enviem a cada jugador el seu ID de Player abans de començar qualsevol altre estat
        for(Player p : players) {
            Login_OUT loginOut = new Login_OUT(p.getId());
            JSONMessage msg = new JSONMessage(this.id, loginOut);
            send(p.getSession(), msg);
            System.out.println("[GameInstance] Enviant LOGIN_OUT al jugador " + p.getId());
        }

        // Assignem estat actual
        currentState = new StateCharacterSelection(this, mapper, repoProvider);
    }

    // Getters + Setters
    public List<Player> getPlayers() {
        return players;
    }

    public void setCurrentState(State currentState) {
        this.currentState = currentState;
    }

    public FloorInfo[] getFloors() {
        return floors;
    }

    public void setFloors(FloorInfo[] floors) {
        this.floors = floors;
    }

    public int getCurrentFloor() {
        return currentFloor;
    }

    public void setCurrentFloor(int currentFloor) {
        this.currentFloor = currentFloor;
    }

    public int getCurrentRoomId() {
        return currentRoomId;
    }

    public void setCurrentRoomId(int currentRoomId) {
        this.currentRoomId = currentRoomId;
    }


    /** Iniciar fil de la partida */
    public void start(){
        if(threadIsRunning.compareAndSet(false, true)){
            System.out.println("The thread for game " + id + " has been started.");
            executor.submit(this::processLoop);
        }
    }

    /** Encuar missatge d'un player */
    public void enqueue(GameMessage message) {
        queue.offer(message);
    }
    /** retorna l'ID de la partida */
    public String getId() {
        return id;
    }

    
    // Aqui van tots els estats del joc
    private void processLoop() {

        while (gameActive) {
            try {
                currentState.tick();
            } catch (Exception e) {
                System.out.println("Error en el game loop:");
                e.printStackTrace();
            }
        }
    }

    /** Funció d'utilitat per enviar un missatge a una connexió concreta */
    public void send(WebSocketSession session, JSONMessage jsonMsg) {
        ObjectMapper mapper = new ObjectMapper();
        send(session, mapper.writeValueAsString(jsonMsg));
        System.out.println("[INFO] Enviant un missatge de tipus "+jsonMsg.messageType+" a la Websocket de "+session.getId());
        System.out.println("\t"+jsonMsg.toString());
    }

    private void send(WebSocketSession session, String text) {
        try {
            synchronized (session) {
                session.sendMessage(new TextMessage(text));
            }
        } catch (IOException e) {
            // handle disconnect
        }
    }


    /** Funció d'utilitat per enviar un missatge a tots els ususaris de la partida */
    public void broadcast(JSONMessage jsonMsg) {
        ObjectMapper mapper = new ObjectMapper();
        broadcast(mapper.writeValueAsString(jsonMsg));
    }

    public void broadcast(String text) {
        for (Player player : players) {
            send(player.getSession(), text);
        }
    }

    public void stop() {
        gameActive = false;
    }

    /**
     * Mètode per llegir un missatge de la cua de missatges entrants al joc
     * @param time de timeout
     * @param timeUnit
     * @return el missatge, o null si no hi ha pendents durant el temps estipulat
     */
    public GameMessage pollMessage(int time, TimeUnit timeUnit) {
        try {
            return queue.poll(time, timeUnit);

        } catch (InterruptedException ex) {
            ex.printStackTrace();
            return null;
        }
    }







    // =====================================================
    // MÈTODES DE FASE DE MAPA
    // =====================================================

    // Responses Servidor

    private void enviarMapUpdate() {
        // @TODO: Enviar l'actualització del mapa als jugadors
    }

    private void enviarNodeEntered() {
        // @TODO: Enviar que s'ha entrat en un node i a quin als jugadors
    }


    // =====================================================
    // MÈTODES DE FASE DE COMBAT
    // =====================================================

    // Requests Servidor

    private void combat_action(JsonNode data) {
        // @TODO: Guardar i gestionar l'acció que rebem del jugador al que l'hi toca
    }


    // Responses Servidor

    private void enviarCombatStart() {
        // @TODO: Enviar als jugadors la llista d'enemics que hi han en el combat que acaben de trobar
    }

    private void enviarActionResult() {
        // @TODO: Enviar als jugadors els resultats de l'acció del jugador que ha actuat
    }

    private void enviarTurnUpdate() {
        // @TODO: Enviar als jugadors a qui l'hi toca ara
    }

    private void enviarCombatEnd() {
        // @TODO: Enviar als jugadors els resultats de la batalla (Victòria o Derrota)
    }


    // =====================================================
    // MÈTODES DE LES HABITACIONS (CHEST / REST)
    // =====================================================

    // Requests Servidor

    private void leave_room(JsonNode data) {
        // @TODO: Gestionar el sortir de la sala en la que es troben els jugadors
    }


    // Responses Servidor

    private void enviarChestLoot() {
        // @TODO: Enviar als jugadors el loot rebut del cofre que han trobat (Només en ChestRoom)
    }

    private void enviarStatsRecovered() {
        // @TODO: Enviar als jugadors els punts de vida i de manà que han restaurat
    }
}
