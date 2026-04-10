package net.iesmila.dam.yumeworld.server.api.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "ENEMY")
@PrimaryKeyJoinColumn(name = "en_yumedant_id")
public class Enemy extends Yumedant {
    @Column(name = "en_given_exp", nullable = false)
    private Integer givenExp;

    @OneToMany(mappedBy = "enemy")
    private List<EnemyDrop> drops;

    @OneToMany(mappedBy = "enemy")
    private List<EnemyFloor> enemyFloors;

    public Integer getGivenExp() { return givenExp; }
    public void setGivenExp(Integer givenExp) { this.givenExp = givenExp; }
}
