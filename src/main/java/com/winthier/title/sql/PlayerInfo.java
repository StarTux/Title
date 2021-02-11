package com.winthier.title.sql;

import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;

@Data @Table(name = "players")
public class PlayerInfo {
    @Id
    private Integer id;
    @Column(nullable = false, unique = true)
    private UUID uuid;
    @Column(length = 32, nullable = true)
    private String title;
    @Column(length = 32, nullable = true)
    private String playerListTitle;
}
