package com.winthier.title.sql;

import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import lombok.Data;

/**
 * Every title has a name and a title string, which is what it's
 * displayed at.
 */
@Data
@Table(name = "unlocked",
       uniqueConstraints = {@UniqueConstraint(columnNames = {"player", "title"})})
public class UnlockedInfo {
    @Id
    Integer id;
    @Column(nullable = false)
    UUID player;
    @Column(nullable = false, length = 32)
    String title;

    public UnlockedInfo() { }

    public UnlockedInfo(final UUID player, final String title) {
        this.player = player;
        this.title = title;
    }
}
