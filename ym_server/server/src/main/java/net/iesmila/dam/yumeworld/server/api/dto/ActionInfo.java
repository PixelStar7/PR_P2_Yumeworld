package net.iesmila.dam.yumeworld.server.api.dto;

/**
 * Informació d'una acció disponible per a un jugador durant el combat.
 * S'envia al client via {@code PLAYER_ACTIONS} al inici de cada combat
 * perquè pugui renderitzar el menú d'accions del torn.
 *
 * Camps:
 *   actionId    — ac_id de la taula ACT (el client l'envia de tornada a PLAYER_ACTION_IN)
 *   name        — nom llegible de l'acció (ac_name)
 *   description — descripció tooltip (ac_desc)
 *   mpCost      — cost en MP per executar l'acció (0 si és gratuïta)
 *   imageUrl    — nom de l'asset d'icona (ac_img), pot ser null
 */
public class ActionInfo {

    public int    actionId;
    public String name;
    public String description;
    public int    mpCost;
    public String imageUrl;

    public ActionInfo() {}

    public ActionInfo(int actionId, String name, String description, int mpCost, String imageUrl) {
        this.actionId    = actionId;
        this.name        = name;
        this.description = description;
        this.mpCost      = mpCost;
        this.imageUrl    = imageUrl;
    }

    /** Construeix un ActionInfo a partir d'un Act de la BD i el seu cost MP calculat. */
    public static ActionInfo from(net.iesmila.dam.yumeworld.server.db.model.Act act, int mpCost) {
        return new ActionInfo(
                act.getId(),
                act.getName(),
                act.getDescription(),
                mpCost,
                act.getImage()
        );
    }
}
