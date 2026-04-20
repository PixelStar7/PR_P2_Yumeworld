package net.iesmila.dam.yumeworld.server.db.model;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "PLAYER")
@PrimaryKeyJoinColumn(name = "pl_yumedant_id")
public class YuCharacter extends Yumedant implements Serializable {
    @Column(name = "pl_exp", nullable = false)
    private Integer exp;

    @ManyToOne
    @JoinColumn(name = "pl_role_id")
    private PlayerRole role;

    public Integer getExp() { return exp; }
    public void setExp(Integer exp) { this.exp = exp; }
    public PlayerRole getRole() { return role; }
    public void setRole(PlayerRole role) { this.role = role; }
}
