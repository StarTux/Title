package com.winthier.title.sql;

import com.winthier.sql.SQLRow.Name;
import com.winthier.sql.SQLRow.NotNull;
import com.winthier.sql.SQLRow.UniqueKey;
import com.winthier.sql.SQLRow;
import java.util.Date;
import java.util.UUID;
import lombok.Data;

/**
 * Every title has a name and a title string, which is what it's
 * displayed at.
 */
@Data @Name("unlocked") @NotNull
@UniqueKey(value = {"player", "title"}, name = "player_title")
public final class UnlockedInfo implements SQLRow {
    @Id
    private Integer id;

    private UUID player;

    @Keyed
    private String title;

    @Default("NOW()")
    private Date time;

    public UnlockedInfo() { }

    public UnlockedInfo(final UUID player, final String title) {
        this.player = player;
        this.title = title;
        this.time = new Date();
    }
}
