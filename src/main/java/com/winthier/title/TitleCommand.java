package com.winthier.title;

import com.cavetale.core.command.AbstractCommand;
import com.cavetale.core.command.CommandContext;
import com.cavetale.core.command.CommandNode;
import com.cavetale.core.command.CommandWarn;
import com.cavetale.core.event.player.PluginPlayerEvent.Detail;
import com.cavetale.core.event.player.PluginPlayerEvent;
import com.cavetale.core.font.DefaultFont;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import static com.cavetale.core.font.Unicode.subscript;
import static com.cavetale.mytems.util.Text.roman;
import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.join;
import static net.kyori.adventure.text.Component.newline;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.JoinConfiguration.noSeparators;
import static net.kyori.adventure.text.JoinConfiguration.separator;
import static net.kyori.adventure.text.event.ClickEvent.changePage;
import static net.kyori.adventure.text.event.ClickEvent.runCommand;
import static net.kyori.adventure.text.event.HoverEvent.showText;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.kyori.adventure.text.format.TextDecoration.*;

public final class TitleCommand extends AbstractCommand<TitlePlugin> {
    protected TitleCommand(final TitlePlugin plugin) {
        super(plugin, "title");
    }

    @Override
    protected void onEnable() {
        rootNode.arguments("<title>")
            .description("Select a title")
            .completers(this::complete)
            .playerCaller(this::title);
        rootNode.addChild("default").denyTabCompletion()
            .description("Choose default title")
            .playerCaller(this::defaultTitle);
    }

    private Session requireSession(Player player) {
        Session session = plugin.findSession(player);
        if (session == null) {
            throw new CommandWarn("Session not found. Please try again later!");
        }
        return session;
    }

    private boolean title(Player player, String[] args) {
        if (args.length > 1) return false;
        if (args.length == 1) {
            select(player, args[0]);
        } else {
            list(player);
        }
        return true;
    }

    private void defaultTitle(Player player) {
        requireSession(player).resetTitle(player);
        player.sendMessage(text("Using default title", AQUA));
    }

    private List<String> complete(CommandContext context, CommandNode node, String arg) {
        if (!context.isPlayer()) return List.of();
        List<String> result = new ArrayList<>();
        String lower = arg.toLowerCase();
        for (Title title : plugin.getPlayerTitles(context.player)) {
            if (title.getName().toLowerCase().contains(lower)) {
                result.add(title.getName());
            }
        }
        if ("default".contains(arg)) {
            result.add("default");
        }
        return result;
    }

    private void select(Player player, String titleName) {
        Session session = requireSession(player);
        Title title = plugin.getTitle(titleName);
        if (title == null || !session.hasTitle(player, title)) {
            throw new CommandWarn("You don't have that title.");
        }
        List<Title> titles = session.getTitles(player);
        if (titles.indexOf(title) == 0) {
            session.resetTitle(player);
        } else {
            session.setTitle(player, title);
        }
        player.sendMessage(text()
                           .append(text("Set title to ", AQUA))
                           .append(title.getTitleTag(player.getUniqueId())));
        PluginPlayerEvent.Name.SELECT_PLAYER_TITLE.make(plugin, player)
            .detail(Detail.NAME, title.getName()).callEvent();
    }

    @AllArgsConstructor
    private static final class BookLine {
        private static final BookLine EMPTY = new BookLine(empty(), null, null);
        private Component component;
        /** This will be absorbed by BookPage and tells which page a link should go to. */
        private final TitleGroup group;
        /** This will be absorbed by BookPage and tells which page a link should go to. */
        private final TitleCategory category;
    }

    @RequiredArgsConstructor
    private static final class BookPage {
        private final List<BookLine> lines;
        private TitleGroup group;
        private final Set<TitleCategory> categories = EnumSet.noneOf(TitleCategory.class);

        private Component build() {
            List<ComponentLike> components = new ArrayList<>(lines.size());
            for (BookLine line : lines) {
                components.add(line.component);
            }
            return join(separator(newline()), components);
        }

        private static List<BookPage> fromLines(List<BookLine> lines) {
            final int lineCount = lines.size();
            final int linesPerPage = 14;
            List<BookPage> pages = new ArrayList<>((lineCount - 1) / linesPerPage + 1);
            for (int i = 0; i < lineCount; i += linesPerPage) {
                List<BookLine> subLines = List.copyOf(lines.subList(i, Math.min(lines.size(), i + linesPerPage)));
                BookPage page = new BookPage(subLines);
                for (BookLine line : subLines) {
                    if (line.group != null) page.group = line.group;
                    if (line.category != null) page.categories.add(line.category);
                }
                pages.add(page);
            }
            lines.clear();
            return pages;
        }

        private static void show(Player player, List<BookPage> bookPages) {
            ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
            book.editMeta(m -> {
                    if (m instanceof BookMeta meta) {
                        meta.author(text("Cavetale"));
                        meta.title(text("Title"));
                        List<Component> pages = new ArrayList<>(bookPages.size());
                        for (BookPage bookPage : bookPages) {
                            pages.add(bookPage.build());
                        }
                        meta.pages(pages);
                    }
                });
            player.openBook(book);
        }
    }

    /**
     * Create a book with all titles. We created as follows:
     *
     * - A TOC of all Groups
     * - For each group, a TOC of all categories
     * - For each group categories, a list of all titles.
     *
     * The TOC lines will be remembered for later in the Links maps so
     * the line number can be inserted.
     *
     * The actual lines where the groups or categories are,
     * respectively will be marked with the corresponding enum so the
     * page containing the page can be located later for that purpose.
     */
    private void list(Player player) {
        PlayerTitleCollection collection = PlayerTitleCollection.of(requireSession(player));
        List<BookPage> bookPages = new ArrayList<>();
        List<BookLine> lines = new ArrayList<>(); // collect, frequently cleared
        Map<TitleGroup, BookLine> groupLinks = new EnumMap<>(TitleGroup.class);
        Map<TitleCategory, BookLine> categoryLinks = new EnumMap<>(TitleCategory.class);
        Map<TitleGroup, Integer> groupCounts = new EnumMap<>(TitleGroup.class);
        Map<TitleCategory, Integer> categoryCounts = new EnumMap<>(TitleCategory.class);
        List<ComponentLike> linkComponents = new ArrayList<>();
        // Make the Group links
        lines.add(new BookLine(text("Your Titles", DARK_AQUA, BOLD), null, null));
        lines.add(BookLine.EMPTY);
        List<PlayerTitleCollection.GroupCollection> groups = collection.allGroups();
        for (int i = 0; i < groups.size(); i += 1) {
            final PlayerTitleCollection.GroupCollection group = groups.get(i);
            final int count = group.countUnlocked();
            groupCounts.put(group.getGroup(), count);
            if (count == 0) continue;
            BookLine line = new BookLine(join(noSeparators(),
                                              text(subscript(roman(i + 1).toLowerCase()) + ". ", DARK_GRAY),
                                              text(group.getGroup().getDisplayName(), DARK_BLUE)),
                                         null, null); // This is a TOC entry!
            groupLinks.put(group.getGroup(), line);
            lines.add(line);
        }
        bookPages.addAll(BookPage.fromLines(lines)); // clear lines
        final UUID uuid = player.getUniqueId();
        for (PlayerTitleCollection.GroupCollection group : groups) {
            if (group.countUnlocked() == 0) continue;
            // Group Header
            lines.add(new BookLine(text(group.getGroup().getDisplayName(), DARK_AQUA, BOLD)
                                   .hoverEvent(showText(join(separator(newline()),
                                                             text(group.getGroup().getDisplayName(), AQUA),
                                                             text("Group", DARK_GRAY, ITALIC),
                                                             text("Go Back", GRAY))))
                                   .clickEvent(changePage(1)),
                                   group.getGroup(), null));
            lines.add(BookLine.EMPTY);
            // Make category TOC
            List<PlayerTitleCollection.CategoryCollection> categories = group.allCategories();
            for (int i = 0; i < categories.size(); i += 1) {
                PlayerTitleCollection.CategoryCollection category = categories.get(i);
                final int count = category.countUnlocked();
                categoryCounts.put(category.getCategory(), count);
                if (count == 0) continue;
                BookLine line = new BookLine(join(noSeparators(),
                                                  text(subscript((i + 1) + ") "), DARK_GRAY),
                                                  text(category.getCategory().getShorthand(), DARK_BLUE)),
                                             null, null); // This is a TOC entry!
                categoryLinks.put(category.getCategory(), line);
                lines.add(line);
            }
            bookPages.addAll(BookPage.fromLines(lines)); // clear
            // Make categories
            for (PlayerTitleCollection.CategoryCollection category : categories) {
                if (category.countUnlocked() == 0) continue;
                // Category Header
                lines.add(new BookLine(text(category.getCategory().getShorthand(), DARK_AQUA, UNDERLINED)
                                       .hoverEvent(showText(join(separator(newline()),
                                                                 text(category.getCategory().getDisplayName(), AQUA),
                                                                 text("Category", DARK_GRAY, ITALIC),
                                                                 text("Go Back", GRAY))))
                                       .clickEvent(changePage(1)),
                                       null, category.getCategory()));
                lines.add(BookLine.EMPTY);
                for (PlayerTitleCollection.CollectedTitle title : category.allTitles()) {
                    if (!title.isUnlocked()) continue;
                    lines.add(new BookLine(DefaultFont.bookmarked(title.getTitle().getTitleComponent(uuid))
                                           .hoverEvent(showText(title.getTitle().getTooltip(uuid)))
                                           .clickEvent(runCommand("/title " + title.getTitle().getName())),
                                           null, null));
                }
                bookPages.addAll(BookPage.fromLines(lines)); // clear
            }
        }
        for (int i = 0; i < bookPages.size(); i += 1) {
            final int pageNo = i + 1;
            BookPage page = bookPages.get(i);
            if (page.group != null) {
                BookLine link = groupLinks.get(page.group);
                if (link != null) {
                    final int count = groupCounts.get(page.group);
                    link.component = link.component
                        .hoverEvent(join(separator(newline()),
                                         text(page.group.getDisplayName(), BLUE),
                                         text("Group", DARK_GRAY, ITALIC),
                                         text("Page " + pageNo, GRAY),
                                         text(count + (count == 1 ? " Title" : " Titles"), GRAY)))
                        .clickEvent(changePage(i + 1));
                }
            }
            for (TitleCategory category : page.categories) {
                BookLine link = categoryLinks.get(category);
                if (link != null) {
                    final int count = categoryCounts.get(category);
                    link.component = link.component
                        .hoverEvent(join(separator(newline()),
                                         text(category.getDisplayName(), BLUE),
                                         text("Category", DARK_GRAY, ITALIC),
                                         text("Page " + pageNo, DARK_GRAY),
                                         text(count + (count == 1 ? " Title" : " Titles"), GRAY)))
                        .clickEvent(changePage(i + 1));
                }
            }
        }
        BookPage.show(player, bookPages);
    }
}
