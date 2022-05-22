package com.winthier.title.sql;

import com.winthier.sql.SQLRow;
import com.winthier.title.Shine;
import com.winthier.title.TitlePlugin;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;

@Data @Table(name = "players")
public final class PlayerInfo implements SQLRow {
    @Id
    private Integer id;
    @Column(nullable = false, unique = true)
    private UUID uuid;
    @Column(length = 32, nullable = true)
    private String title; // null => default
    @Column(length = 32, nullable = true)
    private String shine; // null => title
    @Column(length = 32, nullable = true)
    private String suffix; // null => none

    public PlayerInfo() { }

    public PlayerInfo(final UUID uuid) {
        this.uuid = uuid;
    }

    // Nullable
    public Shine parseShine() {
        return shine == null ? null : Shine.ofKey(shine);
    }

    // Nullable
    public SQLSuffix findSuffix() {
        return suffix == null ? null : TitlePlugin.getInstance().getSuffixes().get(suffix);
    }
}
