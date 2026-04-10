package net.iesmila.dam.yumeworld.server.api.model;

import jakarta.persistence.*;

@Entity
@Table(name = "PARAMETER_VALUE")
public class ParameterValue {
    @EmbeddedId
    private ParameterValueId id;

    @ManyToOne
    @MapsId("actionEffectId")
    @JoinColumn(name = "pv_actioneffect_id")
    private ActionEffect actionEffect;

    @Column(name = "pv_effect_id", nullable = false)
    private Integer effectId;

    @ManyToOne
    @JoinColumns({
        @JoinColumn(name = "pv_parametertype_id", referencedColumnName = "pe_parametertype_id", insertable = false, updatable = false),
        @JoinColumn(name = "pv_effect_id", referencedColumnName = "pe_effect_id", insertable = false, updatable = false)
    })
    private ParameterEffect parameterEffect;

    @Column(name = "pv_string_value", length = 10)
    private String stringValue;

    @Column(name = "pv_num_value")
    private Integer numValue;

    @ManyToOne
    @JoinColumn(name = "pv_altered_state_value")
    private AlteredState alteredStateValue;

    public ParameterValueId getId() { return id; }
    public void setId(ParameterValueId id) { this.id = id; }
    // Add additional getters and setters...
}
