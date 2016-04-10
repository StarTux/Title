package com.winthier.title.sql;

import com.avaje.ebean.annotation.CreatedTimestamp;
import com.avaje.ebean.validation.Length;
import com.avaje.ebean.validation.NotEmpty;
import com.avaje.ebean.validation.NotNull;
import java.util.UUID;
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
    @NotNull UUID player;
    @NotEmpty String title;
}
