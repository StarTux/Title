package com.winthier.title.sql;

import com.winthier.sql.SQLDatabase;
import com.winthier.title.Title;

public final class SQLTest {
    public void test() {
        System.out.println(SQLDatabase.testTableCreation(Title.class));
        System.out.println(SQLDatabase.testTableCreation(UnlockedInfo.class));
        System.out.println(SQLDatabase.testTableCreation(SQLSuffix.class));
        System.out.println(SQLDatabase.testTableCreation(SQLPlayerSuffix.class));
    }
}
