package net.iesmila.dam.yumeworld.server.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class EnemyDropId implements Serializable {
    @Column(name = "ed_item_id")
    private Integer itemId;

    @Column(name = "ed_enemy_id")
    private Integer enemyId;

    public EnemyDropId() {}

    public EnemyDropId(Integer itemId, Integer enemyId) {
        this.itemId = itemId;
        this.enemyId = enemyId;
    }

    public Integer getItemId() { return itemId; }
    public void setItemId(Integer itemId) { this.itemId = itemId; }
    public Integer getEnemyId() { return enemyId; }
    public void setEnemyId(Integer enemyId) { this.enemyId = enemyId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EnemyDropId that = (EnemyDropId) o;
        return Objects.equals(itemId, that.itemId) && Objects.equals(enemyId, that.enemyId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemId, enemyId);
    }
}
