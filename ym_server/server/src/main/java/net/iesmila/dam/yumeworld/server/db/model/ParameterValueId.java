package net.iesmila.dam.yumeworld.server.db.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ParameterValueId implements Serializable {
    @Column(name = "pv_actioneffect_id")
    private Integer actionEffectId;

    @Column(name = "pv_parametertype_id")
    private Integer parameterTypeId;
    
    public ParameterValueId() {}

    public ParameterValueId(Integer actionEffectId, Integer parameterTypeId) {
        this.actionEffectId = actionEffectId;
        this.parameterTypeId = parameterTypeId;
    }

    public Integer getActionEffectId() { return actionEffectId; }
    public void setActionEffectId(Integer actionEffectId) { this.actionEffectId = actionEffectId; }
    public Integer getParameterTypeId() { return parameterTypeId; }
    public void setParameterTypeId(Integer parameterTypeId) { this.parameterTypeId = parameterTypeId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParameterValueId that = (ParameterValueId) o;
        return Objects.equals(actionEffectId, that.actionEffectId) && Objects.equals(parameterTypeId, that.parameterTypeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(actionEffectId, parameterTypeId);
    }
}
