package net.iesmila.dam.yumeworld.server.db.model;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "ITEM")
@PrimaryKeyJoinColumn(name = "it_action_id")
@DiscriminatorValue("I")
public class Item extends Act implements Serializable {
    // Specific item fields or methods if necessary
}
