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

@Configuration
public class DatabaseConfig {

    @Bean
    @Primary
    public DataSource dataSource() throws IOException {
        // Llegim l'arxiu database.json des de l'arrel del projecte
        File file = new File("database.json");
        
        if (!file.exists()) {
            throw new RuntimeException("No s'ha trobat l'arxiu de configuració: database.json a l'arrel del projecte.");
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode config = mapper.readTree(file);

        String ip = config.get("ip").asText();
        int port = config.get("port").asInt();
        String database = config.get("database").asText();
        String username = config.get("username").asText();
        String password = config.get("password").asText();

        String url = String.format("jdbc:mysql://%s:%d/%s?serverTimezone=UTC", ip, port, database);

        return DataSourceBuilder.create()
                .url(url)
                .username(username)
                .password(password)
                .driverClassName("com.mysql.cj.jdbc.Driver")
                .build();
    }
}
