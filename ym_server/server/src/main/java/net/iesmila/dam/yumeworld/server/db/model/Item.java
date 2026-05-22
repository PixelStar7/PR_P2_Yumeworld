package net.iesmila.dam.yumeworld.server.db.model;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ITEM")
@PrimaryKeyJoinColumn(name = "it_action_id")
@DiscriminatorValue("I")
public class Item extends Act implements Serializable {

    @OneToMany(mappedBy = "item", fetch = FetchType.LAZY)
    private List<EnemyDrop> enemyDrops = new ArrayList<>();

    public Item() {
        super();
        // Forcem que el tipus d'acció sigui 'I' segons la restricció chk_ac_type
        this.setType("I");
    }

    /**
     * Mètode d'utilitat per a la lògica de consum des del servidor.
     * Aquí podríem comprovar si l'ítem és d'un sol ús o si té requisits.
     */
    @Transient // Indica a JPA que no busqui aquest camp a la base de dades
    public boolean isConsumable() {
        // En un futur podríem afegir un camp 'it_consumable' al DDL.
        return true;
    }

    public List<EnemyDrop> getEnemyDrops() {
        return enemyDrops;
    }

    @Override
    public String toString() {
        return "Item{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", type=" + getType() +
                '}';
    }


}
