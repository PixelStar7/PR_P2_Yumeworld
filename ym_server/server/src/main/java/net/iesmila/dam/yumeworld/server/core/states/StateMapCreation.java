package net.iesmila.dam.yumeworld.server.core.states;

import net.iesmila.dam.yumeworld.server.api.dto.FloorInfo;
import net.iesmila.dam.yumeworld.server.core.GameInstance;
import net.iesmila.dam.yumeworld.server.db.repository.RepositoryProvider;
import net.iesmila.dam.yumeworld.server.services.IMapGenerationService;
import net.iesmila.dam.yumeworld.server.services.impl.MapGenerationService;
import tools.jackson.databind.ObjectMapper;

/**
 * Estat de transició curt que genera el mapa de la partida un sol cop,
 * el desa al GameInstance i passa a StateMapVisualization al primer tick.
 *
 * IMPORTANT — Per què la transició viu a tick() i NO al constructor:
 *
 * Si féssim setCurrentState(new StateMapVisualization(...)) DINS del constructor
 * de StateMapCreation, l'ordre d'evaluació de Java sobreescriuria el currentState.
 *
 * Causa:
 *   1. StateCharacterSelection fa: setCurrentState(new StateMapCreation(...))
 *   2. Java avalua primer 'new StateMapCreation(...)':
 *      - el constructor de SMC s'executa
 *      - dins, fa setCurrentState(SMV) → currentState = SMV ✓
 *      - el constructor retorna la instància SMC
 *   3. DESPRÉS Java executa el setCurrentState extern amb SMC:
 *      → currentState = SMC ✗ (sobreescriu el SMV!)
 *
 * Solució: el constructor només GENERA i DESA el mapa. La transició es fa
 * al primer tick(), que ja està fora de la cadena de constructors imbricats.
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
    }

    /**
     * Al primer tick fem la transició a StateMapVisualization. Aquí no hi ha
     * cadena de constructors imbricats, així que el setCurrentState no es
     * sobreescriu.
     */
    @Override
    public void tick() {
        System.out.println("[StateMapCreation] [" + game.getId() + "] tick() - Transició a StateMapVisualization.");
        game.setCurrentState(new StateMapVisualization(game, objectMapper, repoProvider));
    }
}
