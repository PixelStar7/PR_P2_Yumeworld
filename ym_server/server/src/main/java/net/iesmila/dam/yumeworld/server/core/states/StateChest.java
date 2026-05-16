package net.iesmila.dam.yumeworld.server.core.states;

import net.iesmila.dam.yumeworld.server.api.dto.LootInfo;
import net.iesmila.dam.yumeworld.server.api.messages.JSONMessage;
import net.iesmila.dam.yumeworld.server.api.messages.generic.out.ActionResult_OUT;
import net.iesmila.dam.yumeworld.server.api.messages.mapexploration.out.ChestInfo_OUT;
import net.iesmila.dam.yumeworld.server.api.messages.rooms.in.OpenChest_IN;
import net.iesmila.dam.yumeworld.server.api.messages.rooms.in.PickItem_IN;
import net.iesmila.dam.yumeworld.server.api.messages.rooms.in.ReadyToLeave_IN;
import net.iesmila.dam.yumeworld.server.api.messages.rooms.out.ChestOpened_OUT;
import net.iesmila.dam.yumeworld.server.api.messages.rooms.out.ItemPicked_OUT;
import net.iesmila.dam.yumeworld.server.api.messages.rooms.out.PlayersReadyToLeave_OUT;
import net.iesmila.dam.yumeworld.server.api.messages.rooms.out.ReturnToMap_OUT;
import net.iesmila.dam.yumeworld.server.api.models.Player;
import net.iesmila.dam.yumeworld.server.core.GameInstance;
import net.iesmila.dam.yumeworld.server.core.GameMessage;
import net.iesmila.dam.yumeworld.server.db.repository.RepositoryProvider;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static net.iesmila.dam.yumeworld.server.core.enums.Codes.*;

/**
 * Estat de la sala del cofre.
 *
 * Flux:
 *   1. Entrada → broadcast ChestInfo_OUT (cofre tancat).
 *   2. El primer jugador clica el cofre → OpenChest_IN.
 *   3. Servidor:
 *      - Valida que el cofre no s'hagi obert ja.
 *      - Genera loot via LootService.generateChestLoot(numPlayers, random).
 *        S'hi crea 1 ítem per jugador (amb repetició permesa si el pool és petit).
 *      - Marca el cofre com obert.
 *      - Broadcast ChestOpened_OUT amb la llista de LootInfo disponibles.
 *   4. Cada jugador envia PickItem_IN per agafar UN sol ítem.
 *      - Validacions: cofre obert; jugador no ha agafat res abans;
 *        ítem disponible al cofre i no agafat ja per ningú.
 *      - Broadcast ItemPicked_OUT amb playerId + itemId.
 *   5. Cada jugador envia ReadyToLeave_IN al seu ritme.
 *      - Broadcast PlayersReadyToLeave_OUT amb la llista actualitzada.
 *   6. Quan tots han enviat ReadyToLeave_IN → broadcast ReturnToMap_OUT
 *      i transició a StateMapVisualization.
 *
 * Regla clau: cada jugador pot agafar com a molt 1 ítem. Si no envia
 * PickItem_IN abans del seu ReadyToLeave_IN, perd l'oportunitat.
 */
public class StateChest extends State {

    private boolean chestOpened = false;
    private List<LootInfo> availableItems = new ArrayList<>(); // contingut del cofre
    private final Set<Integer> pickedItemIds = new HashSet<>();   // ítems ja agafats (no podem repetir)
    private final Set<Long> playersWhoPicked = new HashSet<>();   // jugadors que ja han agafat (1 màx per jugador)
    private final Set<Long> playersReadyToLeave = new HashSet<>();

    public StateChest(GameInstance game, ObjectMapper objectMapper, RepositoryProvider repositoryProvider) {
        super(game, objectMapper, repositoryProvider);

        System.out.println("[StateChest] [" + game.getId() + "] Inicialitzant estat del cofre. Cofre tancat.");
        game.broadcast(new JSONMessage(game.getId(), new ChestInfo_OUT()));
    }

    @Override
    public void tick() {
        GameMessage message = game.pollMessage(5, TimeUnit.SECONDS);
        if (message == null) return;

        try {
            JSONMessage gm = objectMapper.readValue(message.payload(), JSONMessage.class);
            System.out.println("[StateChest] [" + game.getId() + "] TICK - Rebut missatge de tipus: " + gm.messageType + " del jugador " + message.user().getId());

            if (OpenChest_IN.TYPE.equals(gm.messageType)) {
                handleOpenChest(message.user(), gm);
            } else if (PickItem_IN.TYPE.equals(gm.messageType)) {
                handlePickItem(message.user(), gm);
            } else if (ReadyToLeave_IN.TYPE.equals(gm.messageType)) {
                handleReadyToLeave(message.user(), gm);
            } else {
                System.out.println("[StateChest] [" + game.getId() + "] Tipus de missatge desconegut o no esperat en aquest estat: " + gm.messageType);
            }
        } catch (Exception e) {
            System.err.println("[StateChest] [" + game.getId() + "] Error parsejant el missatge: " + e.getMessage());
        }
    }

    /**
     * Obre el cofre (si no està ja obert), genera el seu contingut i el broadcasteja.
     */
    private void handleOpenChest(Player player, JSONMessage gm) {
        OpenChest_IN msg = objectMapper.treeToValue(gm.data, OpenChest_IN.class);

        // Validació: identitat del jugador
        if (player.getId() != msg.playerId) {
            System.out.println("[StateChest] [" + game.getId() + "] FAIL - Sessió " + player.getId() + " diu ser jugador " + msg.playerId);
            game.send(player.getSession(), new JSONMessage(game.getId(),
                    new ActionResult_OUT(false, INVALID_PLAYER.value)));
            return;
        }

        // Validació: cofre ja obert?
        if (chestOpened) {
            System.out.println("[StateChest] [" + game.getId() + "] FAIL - El cofre ja està obert, jugador " + msg.playerId + " hi arriba tard.");
            // TODO: afegir codi CHEST_ALREADY_OPENED a Codes. De moment OK.value perquè el client no peti.
            game.send(player.getSession(), new JSONMessage(game.getId(),
                    new ActionResult_OUT(false, OK.value)));
            return;
        }

        // Generació de loot: 1 ítem per jugador (amb repetició permesa, ja documentat)
        int numItems = game.getPlayers().size();
        availableItems = repoProvider.LootService.generateChestLoot(numItems, game.getRandom());

        if (availableItems.isEmpty()) {
            System.err.println("[StateChest] [" + game.getId() + "] WARN - El cofre s'ha obert però no hi ha ítems al pool. Cofre buit.");
        } else {
            System.out.println("[StateChest] [" + game.getId() + "] Cofre obert per jugador " + msg.playerId + " amb " + availableItems.size() + " ítems disponibles.");
        }

        chestOpened = true;
        game.broadcast(new JSONMessage(game.getId(),
                new ChestOpened_OUT(msg.playerId, availableItems)));
    }

    /**
     * Permet a un jugador agafar 1 ítem del cofre. Cada jugador només pot
     * agafar UN ítem; cada ítem només es pot agafar una vegada.
     */
    private void handlePickItem(Player player, JSONMessage gm) {
        PickItem_IN msg = objectMapper.treeToValue(gm.data, PickItem_IN.class);

        // Validació: identitat
        if (player.getId() != msg.playerId) {
            System.out.println("[StateChest] [" + game.getId() + "] FAIL - Sessió " + player.getId() + " diu ser jugador " + msg.playerId);
            game.send(player.getSession(), new JSONMessage(game.getId(),
                    new ActionResult_OUT(false, INVALID_PLAYER.value)));
            return;
        }

        // Validació: el cofre està obert?
        if (!chestOpened) {
            System.out.println("[StateChest] [" + game.getId() + "] FAIL - Jugador " + msg.playerId + " intenta agafar abans d'obrir el cofre.");
            game.send(player.getSession(), new JSONMessage(game.getId(),
                    new ActionResult_OUT(false, CHEST_NOT_OPEN.value)));
            return;
        }

        // Validació: el jugador ja ha agafat un ítem aquest cofre?
        if (playersWhoPicked.contains(msg.playerId)) {
            System.out.println("[StateChest] [" + game.getId() + "] FAIL - Jugador " + msg.playerId + " ja havia agafat un ítem. Només se'n pot agafar un per cofre.");
            game.send(player.getSession(), new JSONMessage(game.getId(),
                    new ActionResult_OUT(false, ITEM_ALREADY_TAKEN.value)));
            return;
        }

        // Validació: l'ítem existeix al cofre?
        boolean existsInChest = availableItems.stream().anyMatch(i -> i.itemId == msg.itemId);
        if (!existsInChest) {
            System.out.println("[StateChest] [" + game.getId() + "] FAIL - Ítem " + msg.itemId + " no és al cofre.");
            game.send(player.getSession(), new JSONMessage(game.getId(),
                    new ActionResult_OUT(false, INVALID_ITEM.value)));
            return;
        }

        // Validació: l'ítem ja ha estat agafat per algú?
        if (pickedItemIds.contains(msg.itemId)) {
            System.out.println("[StateChest] [" + game.getId() + "] FAIL - Ítem " + msg.itemId + " ja l'ha agafat un altre jugador.");
            game.send(player.getSession(), new JSONMessage(game.getId(),
                    new ActionResult_OUT(false, ITEM_ALREADY_TAKEN.value)));
            return;
        }

        // Èxit: registrem l'agafada i broadcastegem
        pickedItemIds.add(msg.itemId);
        playersWhoPicked.add((long)msg.playerId);

        // Resposta individual de confirmació
        game.send(player.getSession(), new JSONMessage(game.getId(),
                new ActionResult_OUT(true, OK.value)));

        // Broadcast a tothom
        game.broadcast(new JSONMessage(game.getId(),
                new ItemPicked_OUT(msg.playerId, msg.itemId)));
        System.out.println("[StateChest] [" + game.getId() + "] SUCCESS - Jugador " + msg.playerId + " s'ha quedat l'ítem " + msg.itemId + ".");
    }

    /**
     * Registra que un jugador ja està llest per sortir. Quan tots ho estiguin,
     * broadcast ReturnToMap_OUT i transició a StateMapVisualization.
     */
    private void handleReadyToLeave(Player player, JSONMessage gm) {
        ReadyToLeave_IN msg = objectMapper.treeToValue(gm.data, ReadyToLeave_IN.class);

        if (player.getId() != msg.playerId) {
            System.out.println("[StateChest] [" + game.getId() + "] FAIL - Sessió " + player.getId() + " diu ser jugador " + msg.playerId);
            game.send(player.getSession(), new JSONMessage(game.getId(),
                    new ActionResult_OUT(false, INVALID_PLAYER.value)));
            return;
        }

        if (!playersReadyToLeave.add(msg.playerId)) {
            System.out.println("[StateChest] [" + game.getId() + "] Jugador " + msg.playerId + " ja havia confirmat sortir. Ignorat.");
            return;
        }

        System.out.println("[StateChest] [" + game.getId() + "] Jugador " + msg.playerId + " llest per sortir (" + playersReadyToLeave.size() + "/" + game.getPlayers().size() + ").");

        List<Long> readyList = new ArrayList<>(playersReadyToLeave);
        game.broadcast(new JSONMessage(game.getId(), new PlayersReadyToLeave_OUT(readyList)));

        if (playersReadyToLeave.size() >= game.getPlayers().size()) {
            System.out.println("[StateChest] [" + game.getId() + "] >>> TOTS ELS JUGADORS ESTAN LLESTOS PER SORTIR <<<");
            game.broadcast(new JSONMessage(game.getId(), new ReturnToMap_OUT()));
            game.setCurrentState(new StateMapVisualization(game, objectMapper, repoProvider));
        }
    }
}
