package net.iesmila.dam.yumeworld.server.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;

/**
 * Configuració de la base de dades.
 * Aquesta classe llegeix les credencials i paràmetres de connexió des de l'arxiu
 * 'appsettings.json' situat a l'arrel del projecte en lloc d'utilitzar
 * l'application.properties de Spring.
 */
@Configuration
public class DatabaseConfig {

    @Bean
    @Primary
    public DataSource dataSource() throws IOException {
        // Llegim l'arxiu appsettings.json des de l'arrel del projecte
        File file = new File("appsettings.json");
        
        if (!file.exists()) {
            throw new RuntimeException("No s'ha trobat l'arxiu de configuració: appsettings.json a l'arrel del projecte.");
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode config = mapper.readTree(file);

        // Extraiem els valors assegurant-nos que els noms de les claus
        // coincideixen amb l'estructura de l'appsettings.json
        String ip = config.get("database_ip").asText();
        int port = config.get("port").asInt();
        String database = config.get("database_name").asText();
        String username = config.get("username").asText();
        String password = config.get("password").asText();

        // Construïm la URL de connexió JDBC per a MySQL
        String url = String.format("jdbc:mysql://%s:%d/%s?serverTimezone=UTC", ip, port, database);

        return DataSourceBuilder.create()
                .url(url)
                .username(username)
                .password(password)
                .driverClassName("com.mysql.cj.jdbc.Driver")
                .build();
    }
}