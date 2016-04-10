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

@Entity
@Table(name = "players", uniqueConstraints = {@UniqueConstraint(columnNames = {"uuid"})})
@Getter
@Setter
public class PlayerInfo {
    @Id Integer id;
    @NotNull UUID uuid;
    String title;
}
