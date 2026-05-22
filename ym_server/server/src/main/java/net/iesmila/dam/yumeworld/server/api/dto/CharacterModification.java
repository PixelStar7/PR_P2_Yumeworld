package net.iesmila.dam.yumeworld.server.api.dto;

/**
 * Mini-model que representa el canvi d'estat d'un personatge entre dos punts
 * (típicament després d'una acció: combat, descans, etc.). Tots els camps són
 * Integer (no int) per poder enviar només els que han canviat (els altres null).
 *
 * NOTA: tots els camps són públics perquè Jackson els serialitzi correctament.
 * Aquesta és la convenció general dels minimodels al projecte.
 */
public class CharacterModification {
    public Integer id;
    public Integer old_hp;
    public Integer new_hp;
    public Integer old_mp;
    public Integer new_mp;
    public Integer old_exp;
    public Integer new_exp;
    public Integer old_level;
    public Integer new_level;

    // Estats alterats
    // TODO: convertir-ho a un enum quan tinguem la llista d'estats alterats
    // a la BD ben definida (taula ALTERED_STATE o similar).
    public String new_altered_state;

    public CharacterModification() {}
}
