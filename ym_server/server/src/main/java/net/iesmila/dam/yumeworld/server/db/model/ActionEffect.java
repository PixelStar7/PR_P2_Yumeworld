package net.iesmila.dam.yumeworld.server.db.model;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ACTION_EFFECT")
public class ActionEffect implements Serializable {
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

    /**
     * Valors dels paràmetres per a aquest ActionEffect concret.
     * EAGER perquè StateCombat els llegeix fora d'una transacció Spring.
     * Indexats per (ae_id, pt_id), es pot consultar via getId().getParameterTypeId().
     */
    @OneToMany(mappedBy = "actionEffect", fetch = FetchType.EAGER)
    private List<ParameterValue> parameterValues = new ArrayList<>();

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Effect getEffect() { return effect; }
    public void setEffect(Effect effect) { this.effect = effect; }
    public Act getAction() { return action; }
    public void setAction(Act action) { this.action = action; }
    public List<ParameterValue> getParameterValues() { return parameterValues; }
    public void setParameterValues(List<ParameterValue> parameterValues) { this.parameterValues = parameterValues; }
}
