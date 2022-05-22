package com.winthier.title.sql;

import com.winthier.sql.SQLRow;
import java.util.Date;
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
public final class UnlockedInfo implements SQLRow {
    @Id
    private Integer id;
    @Column(nullable = false)
    private UUID player;
    @Column(nullable = false, length = 32)
    private String title;
    @Column(nullable = false, columnDefinition = "DATETIME DEFAULT NOW()")
    private Date time;

    public UnlockedInfo() { }

    public UnlockedInfo(final UUID player, final String title) {
        this.player = player;
        this.title = title;
        this.time = new Date();
    }
}
