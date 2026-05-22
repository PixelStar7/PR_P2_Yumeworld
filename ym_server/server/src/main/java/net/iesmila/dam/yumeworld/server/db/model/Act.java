package net.iesmila.dam.yumeworld.server.db.model;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ACT")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "ac_type", discriminatorType = DiscriminatorType.CHAR)
@DiscriminatorValue("A")
public abstract class Act implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ac_id")
    private Integer id;

    @Column(name = "ac_name", nullable = false)
    private String name;

    @Column(name = "ac_desc", nullable = false)
    private String description;

    @Column(name = "ac_img", length = 500)
    private String image;

    @Column(name = "ac_type", insertable = false, updatable = false)
    private String type;

    /**
     * Efectes lligats a aquesta acció a través de la taula ACTION_EFFECT.
     * Heretat per Item i Ability, permet construir tooltips i aplicar lògica
     * d'efectes des del servei sense un repositori dedicat.
     */
    @OneToMany(mappedBy = "action", fetch = FetchType.LAZY)
    private List<ActionEffect> actionEffects = new ArrayList<>();

    // Getters and setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public List<ActionEffect> getActionEffects() { return actionEffects; }
    public void setActionEffects(List<ActionEffect> actionEffects) { this.actionEffects = actionEffects; }
}
