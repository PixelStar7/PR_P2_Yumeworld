package net.iesmila.dam.yumeworld.server.core.states;

import net.iesmila.dam.yumeworld.server.api.dto.CharacterModification;
import net.iesmila.dam.yumeworld.server.api.dto.CombatRewardsResult;
import net.iesmila.dam.yumeworld.server.api.dto.LootInfo;
import net.iesmila.dam.yumeworld.server.api.messages.generic.out.CharacterUpdate_OUT;
import net.iesmila.dam.yumeworld.server.api.messages.JSONMessage;
import net.iesmila.dam.yumeworld.server.api.messages.combatrewards.out.CombatRewards_OUT;
import net.iesmila.dam.yumeworld.server.api.messages.generic.out.ActionResult_OUT;
import net.iesmila.dam.yumeworld.server.api.messages.chest.in.PickItem_IN;
import net.iesmila.dam.yumeworld.server.api.messages.generic.in.ReadyToLeave_IN;
import net.iesmila.dam.yumeworld.server.api.messages.chest.out.ItemPicked_OUT;
import net.iesmila.dam.yumeworld.server.api.messages.generic.out.PlayersReadyToLeave_OUT;
import net.iesmila.dam.yumeworld.server.api.messages.generic.out.ReturnToMap_OUT;
import net.iesmila.dam.yumeworld.server.api.models.Player;
import net.iesmila.dam.yumeworld.server.api.models.PlayerCharacterState;
import net.iesmila.dam.yumeworld.server.core.GameInstance;
import net.iesmila.dam.yumeworld.server.core.GameMessage;
import net.iesmila.dam.yumeworld.server.db.repository.RepositoryProvider;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static net.iesmila.dam.yumeworld.server.core.enums.Codes.*;

/**
 * Estat on es reparteixen les recompenses després d'un combat normal
 * (no boss). Flux pràcticament idèntic a StateChest:
 *
 *   1. Constructor:
 *      - Aplica l'XP a tots els jugadors immediatament (igual per a tots).
 *      - Broadcast CombatRewards_OUT amb la llista de loot disponible.
 *   2. tick() — processa dos tipus de missatge (reusats del cofre):
 *      - PickItem_IN: cada jugador pot agafar UN ítem dels disponibles.
 *        Es desa al PlayerCharacterState.inventoryItemIds.
 *      - ReadyToLeave_IN: cada jugador confirma la sortida.
 *   3. Quan tots envien ReadyToLeave_IN → broadcast ReturnToMap_OUT
 *      + transició a StateMapVisualization.
 *
 * Diferència amb StateChest: aquí l'XP s'aplica al constructor (de cop),
 * mentre que al cofre no hi ha XP. Tot el flux d'agafar items + sortir
 * és idèntic perquè reusem els mateixos missatges (PickItem_IN,
 * ItemPicked_OUT, ReadyToLeave_IN, PlayersReadyToLeave_OUT,
 * ReturnToMap_OUT).
 */
public class StateCombatRewards extends State {

    private final List<LootInfo> availableItems;        // loot disponible (rebut del StateCombat)
    private final Set<Integer> pickedItemIds = new HashSet<>();
    private final Set<Long> playersWhoPicked = new HashSet<>();
    private final Set<Long> playersReadyToLeave = new HashSet<>();

    public StateCombatRewards(GameInstance game, ObjectMapper objectMapper,
                              RepositoryProvider repoProvider, CombatRewardsResult rewards) {
        super(game, objectMapper, repoProvider);

        this.availableItems = rewards.loot != null ? rewards.loot : new ArrayList<>();

        System.out.println("[StateCombatRewards] [" + game.getId() + "] Inicialitzant. XP=" + rewards.xpGained + ", " + availableItems.size() + " items.");

        // Q2a + Q2b: aplicar XP igual per a tots, immediatament
        applyXpToAllPlayers(rewards.xpGained);

        // Broadcast amb XP + items disponibles
        game.broadcast(new JSONMessage(game.getId(),
                new CombatRewards_OUT(rewards.xpGained, availableItems)));
    }

    private void applyXpToAllPlayers(int xp) {
        Random rng = new Random();
        for (PlayerCharacterState pcs : game.getPlayerStates().values()) {
            List<CharacterModification> levelUps = pcs.addXpAndCheckLevelUp(xp, rng);
            System.out.println("[StateCombatRewards] [" + game.getId() + "] Jugador " + pcs.getPlayerId()
                    + ": +" + xp + " XP → total " + pcs.getExp() + " XP, nivell " + pcs.getLevel()
                    + " (" + levelUps.size() + " pujada(es) de nivell).");
            for (CharacterModification mod : levelUps) {
                game.broadcast(new JSONMessage(game.getId(), new CharacterUpdate_OUT(mod)));
                System.out.println("[StateCombatRewards] [" + game.getId() + "] Jugador " + pcs.getPlayerId()
                        + ": puja al nivell " + mod.new_level + " (HP max " + mod.old_hp + " → " + mod.new_hp
                        + ", MP max " + mod.old_mp + " → " + mod.new_mp + ").");
            }
        }
    }

    @Override
    public void tick() {
        GameMessage message = game.pollMessage(5, TimeUnit.SECONDS);
        if (message == null) return;

        try {
            JSONMessage gm = objectMapper.readValue(message.payload(), JSONMessage.class);
            System.out.println("[StateCombatRewards] [" + game.getId() + "] TICK - " + gm.messageType + " de jugador " + message.user().getId());

            if (PickItem_IN.TYPE.equals(gm.messageType)) {
                handlePickItem(message.user(), gm);
            } else if (ReadyToLeave_IN.TYPE.equals(gm.messageType)) {
                handleReadyToLeave(message.user(), gm);
            } else {
                System.out.println("[StateCombatRewards] [" + game.getId() + "] Tipus de missatge no esperat: " + gm.messageType);
            }
        } catch (Exception e) {
            System.err.println("[StateCombatRewards] [" + game.getId() + "] Error: " + e.getMessage());
        }
    }

    /**
     * Permet a un jugador agafar 1 ítem del loot. Mateixes regles que el cofre:
     * un ítem per jugador, un ítem agafat només una vegada.
     */
    private void handlePickItem(Player player, JSONMessage gm) {
        PickItem_IN msg = objectMapper.treeToValue(gm.data, PickItem_IN.class);

        // Identitat
        if (player.getId() != msg.playerId) {
            game.send(player.getSession(), new JSONMessage(game.getId(),
                    new ActionResult_OUT(false, INVALID_PLAYER.value)));
            return;
        }

        // Jugador ja ha agafat?
        if (playersWhoPicked.contains(msg.playerId)) {
            System.out.println("[StateCombatRewards] [" + game.getId() + "] FAIL - Jugador " + msg.playerId + " ja havia agafat ítem.");
            game.send(player.getSession(), new JSONMessage(game.getId(),
                    new ActionResult_OUT(false, ITEM_ALREADY_TAKEN.value)));
            return;
        }

        // Ítem existeix al loot?
        boolean exists = availableItems.stream().anyMatch(i -> i.itemId == msg.itemId);
        if (!exists) {
            game.send(player.getSession(), new JSONMessage(game.getId(),
                    new ActionResult_OUT(false, INVALID_ITEM.value)));
            return;
        }

        // Ítem ja agafat per algú?
        if (pickedItemIds.contains(msg.itemId)) {
            game.send(player.getSession(), new JSONMessage(game.getId(),
                    new ActionResult_OUT(false, ITEM_ALREADY_TAKEN.value)));
            return;
        }

        // OK: registrem i afegim a l'inventari del jugador
        pickedItemIds.add(msg.itemId);
        playersWhoPicked.add(msg.playerId);

        PlayerCharacterState pcs = game.getPlayerState(msg.playerId);
        if (pcs != null) {
            pcs.addItemToInventory(msg.itemId);
        }

        game.send(player.getSession(), new JSONMessage(game.getId(),
                new ActionResult_OUT(true, OK.value)));
        game.broadcast(new JSONMessage(game.getId(),
                new ItemPicked_OUT(msg.playerId, msg.itemId)));
        System.out.println("[StateCombatRewards] [" + game.getId() + "] SUCCESS - Jugador " + msg.playerId + " s'ha quedat l'ítem " + msg.itemId + ".");
    }

    /**
     * Registra confirmació de sortida. Quan tots ho han fet, transició al mapa.
     */
    private void handleReadyToLeave(Player player, JSONMessage gm) {
        // Usem player.getId() directament: és la font autenticada via WebSocket.
        // El client envia data:{} buit, no cal deserialitzar ReadyToLeave_IN.
        long playerId = player.getId();

        if (!playersReadyToLeave.add(playerId)) {
            System.out.println("[StateCombatRewards] [" + game.getId() + "] Jugador " + playerId + " ja havia confirmat sortir.");
            return;
        }

        System.out.println("[StateCombatRewards] [" + game.getId() + "] Jugador " + playerId + " llest per sortir (" + playersReadyToLeave.size() + "/" + game.getPlayers().size() + ").");

        game.broadcast(new JSONMessage(game.getId(),
                new PlayersReadyToLeave_OUT(new ArrayList<>(playersReadyToLeave))));

        if (playersReadyToLeave.size() >= game.getPlayers().size()) {
            System.out.println("[StateCombatRewards] [" + game.getId() + "] >>> TOTS LLESTOS, TORNEM AL MAPA <<<");
            game.broadcast(new JSONMessage(game.getId(), new ReturnToMap_OUT()));
            game.setCurrentState(new StateMapVisualization(game, objectMapper, repoProvider));
        }
    }
}
