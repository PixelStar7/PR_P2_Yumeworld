package net.iesmila.dam.yumeworld.server.db.model;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "ENEMY_BIOMES")
public class EnemyBiome implements Serializable {
    @EmbeddedId
    private EnemyBiomeId id;

    @ManyToOne
    @MapsId("enemyId")
    @JoinColumn(name = "enbi_enemy_id")
    private Enemy enemy;

    @ManyToOne
    @MapsId("biomeId")
    @JoinColumn(name = "enbi_biome_id")
    private Biome biome;

    @Column(name = "enbi_quantity", nullable = false)
    private Integer quantity;

    public EnemyBiomeId getId() { return id; }
    public void setId(EnemyBiomeId id) { this.id = id; }
    public Enemy getEnemy() { return enemy; }
    public void setEnemy(Enemy enemy) { this.enemy = enemy; }
    public Biome getBiome() { return biome; }
    public void setBiome(Biome biome) { this.biome = biome; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
