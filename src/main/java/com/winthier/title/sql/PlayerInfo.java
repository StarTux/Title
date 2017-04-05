package com.winthier.title.sql;

import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "players",
       uniqueConstraints = {@UniqueConstraint(columnNames = {"uuid"})})
@Getter
@Setter
public class PlayerInfo {
    @Id Integer id;
    @Column(nullable = false) UUID uuid;
    String title;
}
