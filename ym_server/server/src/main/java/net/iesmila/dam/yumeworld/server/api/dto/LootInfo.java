package net.iesmila.dam.yumeworld.server.api.dto;

import net.iesmila.dam.yumeworld.server.db.model.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO d'un objecte que cau al jugador (cofre o drop d'enemic).
 * Conté la informació necessària perquè el client pugui mostrar-lo i
 * el jugador pugui interactuar-hi (PickItem_IN fa servir itemId).
 */
public class LootInfo {

    public int itemId;
    public String name;
    public String description;
    public String imageUrl;
    public List<EffectInfo> effects;

    public LootInfo() {}

    public LootInfo(int itemId, String name, String description, String imageUrl, List<EffectInfo> effects) {
        this.itemId = itemId;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.effects = effects;
    }

    /**
     * Factory: converteix una entitat Item de la BD a LootInfo serialitzable.
     * Inclou tots els efectes lligats via ACTION_EFFECT.
     *
     * IMPORTANT: el caller ha d'assegurar-se que els actionEffects ja estan
     * carregats (via JOIN FETCH al query), o que la invocació es fa dins
     * d'una transacció activa, per evitar LazyInitializationException.
     */
    public static LootInfo fromItem(Item item) {
        List<EffectInfo> effects;
        if (item.getActionEffects() == null) {
            effects = new ArrayList<>();
        } else {
            effects = item.getActionEffects().stream()
                    .map(ae -> EffectInfo.fromEffect(ae.getEffect()))
                    .collect(Collectors.toList());
        }

        return new LootInfo(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getImage(),
                effects
        );
    }
}
