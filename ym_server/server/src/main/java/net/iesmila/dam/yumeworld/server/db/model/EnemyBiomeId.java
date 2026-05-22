package net.iesmila.dam.yumeworld.server.db.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class EnemyBiomeId implements Serializable {
    @Column(name = "enbi_enemy_id")
    private Integer enemyId;

    @Column(name = "enbi_biome_id")
    private Integer biomeId;

    public EnemyBiomeId() {}

    public EnemyBiomeId(Integer enemyId, Integer biomeId) {
        this.enemyId = enemyId;
        this.biomeId = biomeId;
    }

    public Integer getEnemyId() { return enemyId; }
    public void setEnemyId(Integer enemyId) { this.enemyId = enemyId; }
    public Integer getBiomeId() { return biomeId; }
    public void setBiomeId(Integer biomeId) { this.biomeId = biomeId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EnemyBiomeId that = (EnemyBiomeId) o;
        return Objects.equals(enemyId, that.enemyId) && Objects.equals(biomeId, that.biomeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(enemyId, biomeId);
    }
}
