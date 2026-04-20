package net.iesmila.dam.yumeworld.server.db.model;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "ENEMY")
@PrimaryKeyJoinColumn(name = "en_yumedant_id")
public class Enemy extends Yumedant implements Serializable {
    @Column(name = "en_given_exp", nullable = false)
    private Integer givenExp;

    @OneToMany(mappedBy = "enemy")
    private List<EnemyDrop> drops;

    @OneToMany(mappedBy = "enemy")
    private List<EnemyFloor> enemyFloors;

    public Integer getGivenExp() { return givenExp; }
    public void setGivenExp(Integer givenExp) { this.givenExp = givenExp; }
}
