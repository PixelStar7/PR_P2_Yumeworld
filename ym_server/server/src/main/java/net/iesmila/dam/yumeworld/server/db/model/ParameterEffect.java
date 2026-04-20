package net.iesmila.dam.yumeworld.server.db.model;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "PARAMETER_EFFECT")
public class ParameterEffect implements Serializable {
    @EmbeddedId
    private ParameterEffectId id;

    @ManyToOne
    @MapsId("parameterTypeId")
    @JoinColumn(name = "pe_parametertype_id")
    private ParameterType parameterType;

    @ManyToOne
    @MapsId("effectId")
    @JoinColumn(name = "pe_effect_id")
    private Effect effect;

    public ParameterEffectId getId() { return id; }
    public void setId(ParameterEffectId id) { this.id = id; }
    public ParameterType getParameterType() { return parameterType; }
    public void setParameterType(ParameterType parameterType) { this.parameterType = parameterType; }
    public Effect getEffect() { return effect; }
    public void setEffect(Effect effect) { this.effect = effect; }
}
