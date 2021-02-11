package com.winthier.title.sql;

import com.winthier.title.Title;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;

/**
 * Every title has a name and a title string, which is what it's
 * displayed at.
 */
@Data
@Table(name = "titles")
public final class TitleInfo implements Title {
    @Id
    private Integer id;
    @Column(nullable = false, length = 32, unique = true)
    private String name;
    @Column(nullable = false, length = 255)
    private String title;
    @Column(nullable = true, length = 255)
    private String description;
    @Column(nullable = false, columnDefinition = "INT(3) DEFAULT 0")
    private int priority;
    @Column(nullable = true, length = 4096)
    private String titleJson;
    @Column(nullable = true, length = 255)
    private String playerListPrefix;
    @Column(nullable = true, length = 255)
    private String shine;

    public TitleInfo() { }

    public TitleInfo(final String name, final String title, final String description) {
        this.name = name;
        this.title = title;
        this.description = description;
    }

    public Title toTitle() {
        return this;
    }
}
