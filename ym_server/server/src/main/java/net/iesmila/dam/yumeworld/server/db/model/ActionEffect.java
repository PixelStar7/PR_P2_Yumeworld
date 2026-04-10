package net.iesmila.dam.yumeworld.server.db.model;

import jakarta.persistence.*;

@Entity
@Table(name = "ACTION_EFFECT")
public class ActionEffect {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ae_id")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "ae_effect_id", nullable = false)
    private Effect effect;

    @ManyToOne
    @JoinColumn(name = "ae_action_id", nullable = false)
    private Act action;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Effect getEffect() { return effect; }
    public void setEffect(Effect effect) { this.effect = effect; }
    public Act getAction() { return action; }
    public void setAction(Act action) { this.action = action; }
}
