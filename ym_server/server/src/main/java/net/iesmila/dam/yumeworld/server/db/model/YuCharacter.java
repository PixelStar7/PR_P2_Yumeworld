package net.iesmila.dam.yumeworld.server.db.model;

import jakarta.persistence.*;

@Entity
@Table(name = "PLAYER")
@PrimaryKeyJoinColumn(name = "pl_yumedant_id")
public class Character extends Yumedant {
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
