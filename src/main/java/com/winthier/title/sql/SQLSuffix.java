package com.winthier.title.sql;

import com.cavetale.core.font.Emoji;
import com.cavetale.core.font.Unicode;
import com.cavetale.mytems.Mytems;
import com.winthier.sql.SQLRow;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;
import net.kyori.adventure.text.Component;
import static net.kyori.adventure.text.Component.text;

@Data @Table(name = "suffixes")
public final class SQLSuffix implements SQLRow, Comparable<SQLSuffix> {
    @Id
    private Integer id;
    @Column(nullable = false, length = 32, unique = true)
    private String name;
    @Column(nullable = false, length = 255)
    private String format;
    @Column(nullable = false, columnDefinition = "INT(3) DEFAULT 0")
    private int priority;
    @Column(nullable = true, length = 32)
    private String category;
    private transient Mytems mytems;
    private transient Component component;
    private transient boolean partOfName;
    private transient char character = (char) 0;
    private transient boolean invalid;

    public SQLSuffix() { }

    public SQLSuffix(final String name, final String format) {
        this.name = name;
        this.format = format;
    }

    @Override
    public int compareTo(SQLSuffix other) {
        return Integer.compare(other.priority, priority);
    }

    private void invalidate() {
        invalid = true;
        component = Component.empty();
        partOfName = false;
        character = (char) 0;
    }

    public void unpack() {
        this.invalid = false;
        if (format.startsWith(":") && format.endsWith(":")) {
            String key = format.substring(1, format.length() - 1);
            Emoji emoji = Emoji.getEmoji(key);
            if (emoji == null) {
                invalidate();
                return;
            }
            this.component = emoji.component;
            if (emoji.isUnicode()) {
                this.partOfName = true;
                this.character = ((Unicode) emoji.getEnume()).character;
            } else {
                this.partOfName = false;
                this.character = (char) 0;
            }
        } else if (format.length() == 1) {
            this.partOfName = true;
            this.component = text(format);
            this.character = format.charAt(0);
        } else {
            invalidate();
        }
    }

    public boolean is(SQLSuffix other) {
        return other != null && name.equals(other.name);
    }

    public Mytems getMytems() {
        if (format.startsWith(":") && format.endsWith(":")) {
            return Mytems.forId(format.substring(1, format.length() - 1));
        } else {
            return null;
        }
    }
}
