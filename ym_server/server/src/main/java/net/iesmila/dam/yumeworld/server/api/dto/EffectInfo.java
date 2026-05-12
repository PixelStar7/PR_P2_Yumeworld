package net.iesmila.dam.yumeworld.server.api.dto;

import net.iesmila.dam.yumeworld.server.db.model.Effect;

/**
 * DTO mínim per a un efecte lligat a una acció (item o habilitat).
 * Reflecteix les dades de la taula EFFECT que han de viatjar al client
 * per renderitzar tooltips o icones.
 *
 * TODO: ampliar amb paràmetres (magnitude, duration, etc.) extrets de
 * PARAMETER_VALUE quan calgui. Per ara només l'identificador i el nom.
 */
public class EffectInfo {

    public int effectId;
    public String name;

    public EffectInfo() {}

    public EffectInfo(int effectId, String name) {
        this.effectId = effectId;
        this.name = name;
    }

    /** Factory: converteix una entitat Effect de la BD a EffectInfo serialitzable. */
    public static EffectInfo fromEffect(Effect effect) {
        return new EffectInfo(effect.getId(), effect.getName());
    }
}
