package net.iesmila.dam.yumeworld.server.api.states;

import net.iesmila.dam.yumeworld.server.api.messages.mapexploration.minimodels.FloorInfo;
import net.iesmila.dam.yumeworld.server.core.GameInstance;
import net.iesmila.dam.yumeworld.server.db.repository.RepositoryProvider;
import net.iesmila.dam.yumeworld.server.services.IMapGenerationService;
import net.iesmila.dam.yumeworld.server.services.MapGenerationService;
import tools.jackson.databind.ObjectMapper;

/**
 * Estat de transició curt que genera el mapa de la partida un sol cop,
 * el desa al GameInstance i passa immediatament a StateMapVisualization.
 * No espera cap missatge dels clients: tot el repartiment de biomes i
 * tipus de sala el resol el MapGenerationService.
 */
public class StateMapCreation extends State {

    private final int numFloors = 5;     // Nombre de pisos del mapa
    private final int numColumnes = 4;   // Nombre de nodes per pis (només se'n visita un)

    public StateMapCreation(GameInstance game, ObjectMapper om, RepositoryProvider rp) {
        super(game, om, rp);

        System.out.println("[StateMapCreation] [" + game.getId() + "] Inicialitzant generació del mapa.");

        IMapGenerationService mapGenerationService = new MapGenerationService();
        FloorInfo[] floors = new FloorInfo[numFloors];

        // El bioma del pis l'assigna el servei a partir de la posició (floorNum, totalFloors).
        // Cada pis és una única fila de nodes (només se'n visita un).
        // L'últim pis és el pis del boss i té un únic node, al qual hi convergeixen
        // tots els nodes del pis anterior (la regla d'adjacència ho fa automàticament:
        // qualsevol node del pis N pot anar al node 0 del pis N+1 si N+1 només té 1 node).
        for (int i = 0; i < numFloors; i++) {
            int columnsAquestPis = (i == numFloors - 1) ? 1 : numColumnes;
            floors[i] = mapGenerationService.generateFloor(i, numFloors, columnsAquestPis);
        }

        // Persistim el mapa a la partida perquè StateMapVisualization (i estats posteriors)
        // el puguin consultar sense haver de regenerar-lo.
        game.setFloors(floors);
        System.out.println("[StateMapCreation] [" + game.getId() + "] Mapa generat amb " + numFloors + " pisos i guardat al GameInstance.");

        // Transició immediata a la visualització del mapa.
        System.out.println("[StateMapCreation] [" + game.getId() + "] Transició a StateMapVisualization.");
        game.setCurrentState(new StateMapVisualization(game, objectMapper, repoProvider));
    }

    /**
     * Aquest estat només viu el temps que tarda el constructor.
     * Si per qualsevol raó el processLoop crida tick() abans de la transició,
     * no fem res: el mapa ja s'ha generat i el següent tick anirà al nou estat.
     */
    @Override
    public void tick() {
        // No-op: l'estat és transitori i s'ha de substituir abans del primer tick.
    }
}
