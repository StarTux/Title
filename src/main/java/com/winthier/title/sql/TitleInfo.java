package com.winthier.title.sql;

import com.winthier.title.Title;
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
@Entity
@Table(name = "titles", uniqueConstraints = {@UniqueConstraint(columnNames = {"name"})})
@Getter
@Setter
public final class TitleInfo {
    @Id Integer id;
    @Column(nullable = false, length = 32) String name;
    @Column(nullable = false, length = 64) String title;
    String description;

    public Title toTitle() {
        return new Title(name, title, description);
    }
}
