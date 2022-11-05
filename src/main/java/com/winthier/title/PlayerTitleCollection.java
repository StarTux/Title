package com.winthier.title;

import com.winthier.title.sql.UnlockedInfo;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
public final class PlayerTitleCollection {
    protected final Map<TitleGroup, GroupCollection> groupMap = new EnumMap<>(TitleGroup.class);
    //protected final CategoryCollection favorites = new CategoryCollection(TitleCategory.UNKNOWN);

    public int count() {
        int count = 0;
        for (GroupCollection it : groupMap.values()) {
            count += it.count();
        }
        return count;
    }

    public int countUnlocked() {
        int count = 0;
        for (GroupCollection it : groupMap.values()) {
            count += it.countUnlocked();
        }
        return count;
    }

    @Getter @RequiredArgsConstructor
    public static final class GroupCollection {
        protected final TitleGroup group;
        protected final Map<TitleCategory, CategoryCollection> categoryMap = new EnumMap<>(TitleCategory.class);

        public int count() {
            int count = 0;
            for (CategoryCollection it : categoryMap.values()) {
                count += it.count();
            }
            return count;
        }

        public int countUnlocked() {
            int count = 0;
            for (CategoryCollection it : categoryMap.values()) {
                count += it.countUnlocked();
            }
            return count;
        }

        public List<CategoryCollection> allCategories() {
            List<CategoryCollection> result = new ArrayList<>(categoryMap.values());
            result.sort((a, b) -> Integer.compare(a.category.ordinal(), b.category.ordinal()));
            return result;
        }
    }

    @Getter @RequiredArgsConstructor
    public static final class CategoryCollection {
        protected final TitleCategory category;
        protected final List<CollectedTitle> titlesList = new ArrayList<>();

        public int count() {
            return titlesList.size();
        }

        public int countUnlocked() {
            int count = 0;
            for (CollectedTitle it : titlesList) {
                if (it.isUnlocked()) count += 1;
            }
            return count;
        }

        public List<CollectedTitle> allTitles() {
            List<CollectedTitle> result = new ArrayList<>(titlesList);
            titlesList.sort((a, b) -> a.title.compareTo(b.title));
            return result;
        }
    }

    @Getter @RequiredArgsConstructor
    public static final class CollectedTitle {
        protected final Title title;
        protected final UnlockedInfo unlockedInfo;
        protected final boolean unlocked;
        protected final boolean favorite;
    }

    public void load(Session session) {
        for (Title title : TitlePlugin.getInstance().getTitles()) {
            UnlockedInfo unlockedInfo = session.unlockedRows.get(title.getName());
            TitleCategory category = title.parseCategory();
            final boolean isUnlocked = unlockedInfo != null || title.hasPermission(session.uuid);
            if (!isUnlocked && category == TitleCategory.HIDDEN) continue;
            if (!isUnlocked && category == TitleCategory.LEGACY) continue;
            final boolean isFavorite = false;
            final CollectedTitle collected = new CollectedTitle(title, unlockedInfo, isUnlocked, isFavorite);
            groupMap.computeIfAbsent(category.group, t -> new GroupCollection(t))
                .categoryMap.computeIfAbsent(category, c -> new CategoryCollection(c))
                .titlesList.add(collected);
        }
    }

    public static PlayerTitleCollection of(Session session) {
        PlayerTitleCollection result = new PlayerTitleCollection();
        result.load(session);
        return result;
    }

    public List<GroupCollection> allGroups() {
        List<GroupCollection> list = new ArrayList<>(groupMap.values());
        list.sort((a, b) -> Integer.compare(a.getGroup().ordinal(), b.getGroup().ordinal()));
        return list;
    }
}
