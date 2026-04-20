package net.iesmila.dam.yumeworld.server.components;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

public class ProvaServidorSimple {

    // ====================================================
    // 1. L'ENUM D'ESTATS (Per saber on som del diagrama)
    // ====================================================
    public enum GameState {
        CHARACTER_SELECTION,
        MAP_EXPLORATION
    }

    // ====================================================
    // 2. EL RECORD (La nostra caixa de dades màgica i ràpida)
    // ====================================================
    public record PlayerReadyData(int characterId, int roleId) {}

    // ====================================================
    // 3. LA NOSTRA SIMULACIÓ (Fes Run aquí!)
    // ====================================================
    public static void main(String[] args) {

        // Eines necessàries
        ObjectMapper objectMapper = new ObjectMapper();

        // Simulem que la partida acaba de començar
        GameState currentState = GameState.CHARACTER_SELECTION;

        System.out.println("--- INICIANT SIMULACIÓ DEL SERVIDOR ---");
        System.out.println("Estat actual del joc: " + currentState);
        System.out.println("Esperant missatges...\n");

        // -----------------------------------------------------------------
        // Simulem un missatge JSON exactament igual al que enviaria Unity/Godot
        // -----------------------------------------------------------------
        String missatgeRebutDelClient = """
            {
              "action": "PLAYER_READY",
              "data": {
                "characterId": 3,
                "roleId": 1
              }
            }
            """;

        try {
            // Pas A: Llegim el text (String) i el convertim a JSON real
            JsonNode json = objectMapper.readTree(missatgeRebutDelClient);

            // Pas B: Separem l'acció de les dades
            String action = json.has("action") ? json.get("action").asText() : "UNKNOWN";
            JsonNode data = json.has("data") ? json.get("data") : null;

            // Pas C: El nostre Switch Central
            switch (action) {

                case "PLAYER_READY":
                    // Blidem el codi: Només fem cas si estem a la fase de selecció!
                    if (currentState == GameState.CHARACTER_SELECTION) {

                        // ✨ LA MÀGIA: Convertim el bloc "data" al nostre Record automàticament! ✨
                        PlayerReadyData readyData = objectMapper.treeToValue(data, PlayerReadyData.class);

                        System.out.println("✅ [ÈXIT] Hem rebut l'acció PLAYER_READY!");
                        System.out.println("   -> El jugador ha triat el Yumedant ID: " + readyData.characterId());
                        System.out.println("   -> El jugador ha triat el Rol ID: " + readyData.roleId());

                        // Aquí és on tu guardaries això a la base de dades o a la memòria de la partida

                    } else {
                        System.out.println("❌ [AVÍS] Acció ignorada perquè no estem a la fase de selecció.");
                    }
                    break;

                case "GAME_START_REQUEST":
                    // Blidem el codi: Només fem cas si estem a la fase de selecció!
                    if (currentState == GameState.CHARACTER_SELECTION) {

                        System.out.println("Volen començar el game!");



                        // Aquí és on tu guardaries això a la base de dades o a la memòria de la partida

                    } else {
                        System.out.println("❌ [AVÍS] Acció ignorada perquè no estem a la fase de selecció.");
                    }
                    break;

                default:
                    System.out.println("Acció desconeguda.");
                    break;
            }

        } catch (Exception e) {
            System.out.println("Ups! Hi ha hagut un error llegint el JSON: " + e.getMessage());
        }
    }
}