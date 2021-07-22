package com.winthier.title.sql;

import java.util.Date;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import lombok.Data;

/**
 * Player suffix (badge) unlocks.
 */
@Data
@Table(name = "player_suffixes",
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"player", "suffix"})
       })
public final class SQLPlayerSuffix {
    @Id
    private Integer id;
    @Column(nullable = false)
    private UUID player;
    /**
     * The name or category. If this variable starts with '#', it is
     * considered a category.
     */
    @Column(nullable = false, length = 32)
    private String suffix;
    @Column(nullable = false, columnDefinition = "DATETIME DEFAULT NOW()")
    private Date time;

    public SQLPlayerSuffix() { }

    public SQLPlayerSuffix(final UUID player, final String suffix) {
        this.player = player;
        this.suffix = suffix;
        this.time = new Date();
    }
}
