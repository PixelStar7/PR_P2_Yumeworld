package net.iesmila.dam.yumeworld.server.db.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ParameterEffectId implements Serializable {
    @Column(name = "pe_parametertype_id")
    private Integer parameterTypeId;

    @Column(name = "pe_effect_id")
    private Integer effectId;

    public ParameterEffectId() {}

    public ParameterEffectId(Integer parameterTypeId, Integer effectId) {
        this.parameterTypeId = parameterTypeId;
        this.effectId = effectId;
    }

    public Integer getParameterTypeId() { return parameterTypeId; }
    public void setParameterTypeId(Integer parameterTypeId) { this.parameterTypeId = parameterTypeId; }
    public Integer getEffectId() { return effectId; }
    public void setEffectId(Integer effectId) { this.effectId = effectId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParameterEffectId that = (ParameterEffectId) o;
        return Objects.equals(parameterTypeId, that.parameterTypeId) && Objects.equals(effectId, that.effectId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parameterTypeId, effectId);
    }
}
