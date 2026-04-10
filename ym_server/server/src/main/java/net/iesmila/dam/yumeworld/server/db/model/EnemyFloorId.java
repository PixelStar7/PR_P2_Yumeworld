package net.iesmila.dam.yumeworld.server.db.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class EnemyFloorId implements Serializable {
    @Column(name = "enfl_enemy_id")
    private Integer enemyId;

    @Column(name = "enfl_floor_id")
    private Integer floorId;

    public EnemyFloorId() {}

    public EnemyFloorId(Integer enemyId, Integer floorId) {
        this.enemyId = enemyId;
        this.floorId = floorId;
    }

    public Integer getEnemyId() { return enemyId; }
    public void setEnemyId(Integer enemyId) { this.enemyId = enemyId; }
    public Integer getFloorId() { return floorId; }
    public void setFloorId(Integer floorId) { this.floorId = floorId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EnemyFloorId that = (EnemyFloorId) o;
        return Objects.equals(enemyId, that.enemyId) && Objects.equals(floorId, that.floorId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(enemyId, floorId);
    }
}
