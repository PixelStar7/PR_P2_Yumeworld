# Yumeworld Spring Boot Server

Aquest és el backend del projecte **Yumeworld**, desenvolupat en Java amb **Spring Boot**. El servidor gestiona la lògica de la partida, la comunicació en temps real via WebSockets amb el client Unity i la persistència de dades utilitzant Spring Data JPA/Hibernate connectat a una base de dades MySQL.

## Configuració de la Base de Dades

Per facilitar la modificació de credencials i paràmetres de connexió (com per exemple quan passem de l'entorn de desenvolupament a producció o entre diferents membres de l'equip), la configuració de la base de dades s'ha extret fora dels arxius estàndards de Spring (`application.properties`) i s'ha centralitzat en un fitxer JSON independent anomenat `appsettings.json`.

### Arxiu `appsettings.json`

Aquest arxiu s'ha de col·locar a l'arrel del projecte (al mateix nivell que el fitxer `pom.xml`). La classe `DatabaseConfig.java` s'encarregarà de llegir-lo automàticament a l'inici de l'aplicació per muntar la connexió amb MySQL.

L'estructura que espera el servidor és la següent:

```json
{
  "database_ip": "localhost",
  "port": 3306,
  "database_name": "yumeworld",
  "username": "root",
  "password": "root"
}
```

* **`database_ip`**: IP on es troba el servidor MySQL (per defecte `localhost`).
* **`port`**: Port d'escolta del MySQL (per defecte `3306`).
* **`database_name`**: Nom exacte de la base de dades creada a MySQL (`yumeworld`).
* **`username`**: Usuari amb permisos sobre la base de dades.
* **`password`**: Contrasenya de l'usuari.

### Com funciona internament?

1. En lloc de llegir les propietats `spring.datasource.*`, hem creat un Bean personalitzat a la classe `net.iesmila.dam.yumeworld.server.config.DatabaseConfig`.
2. Aquesta classe utilitza `ObjectMapper` de Jackson Databind per parsejar el fitxer `appsettings.json`.
3. Es llegeixen les claus (per exemple, `config.get("database_ip").asText()`) i es genera la URL completa JDBC (`jdbc:mysql://<ip>:<port>/<database_name>?serverTimezone=UTC`).
4. Finalment, es crea el `DataSource` que Spring i Hibernate utilitzaran per a tota l'aplicació.

Si el fitxer `appsettings.json` no es troba a l'arrel del projecte, l'aplicació llançarà una excepció en arrancar i s'aturarà indicant que no el pot trobar.