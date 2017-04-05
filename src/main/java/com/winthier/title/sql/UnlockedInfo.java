package com.winthier.title.sql;

import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

/**
 * Every title has a name and a title string, which is what it's
 * displayed at.
 */
@Entity()
@Table(name = "unlocked", uniqueConstraints = {@UniqueConstraint(columnNames = {"player", "title"})})
@Getter
@Setter
public class UnlockedInfo {
    @Id Integer id;
    @Column(nullable = false) UUID player;
    @Column(nullable = false) String title;
}
