package net.iesmila.dam.yumeworld.server.db.repository;

import net.iesmila.dam.yumeworld.server.services.impl.LootService;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Provider de beans (repositoris i serveis) injectats per Spring que passem
 * als estats del joc. Tot i el nom "Repository", també hi posem serveis que
 * necessiten @Autowired (com LootService) perquè els estats hi tinguin accés
 * sense haver-los d'instanciar manualment (i perdre les seves dependències).
 *
 * TODO: si la llista de serveis creix, considerar dividir en RepositoryProvider
 * + ServiceProvider per mantenir la coherència semàntica.
 */
@Component
public class RepositoryProvider {
    // Repositoris
    @Autowired
    public YuCharacterRepository RepoYuCharacter;

    @Autowired
    public ItemRepository RepoItem;

    @Autowired
    public EnemyRepository RepoEnemy;

    // Serveis
    @Autowired
    public LootService LootService;
}
