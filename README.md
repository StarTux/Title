# Title

Title is a Paper plugin for collecting and wearing titles on
Cavetale. Players open an in-game book with `/title` to browse ranks,
event rewards, and seasonal titles, see how many they have collected,
and click an unlocked title to wear it.

A title can add a prefix, change the color of the player's name, or
supply a **shine** that appears while gliding, after an ender pearl
teleport, or on an arrow hit. Players can also wear a **badge** after
their name. The admin commands call badges *suffixes*. Title updates
the player's display name, tab-list entry, and overhead name tag on
custom scoreboards.

## Commands

| Command | Permission | Description |
| --- | --- | --- |
| `/title` | `title.title` | Open the collection book and browse titles by group and category. |
| `/title my` | `title.title` | Open a book of just your unlocked titles, sorted by name. |
| `/title <title>` | `title.title` | Wear an unlocked title. |
| `/title default` | `title.title` | Use the first unlocked title in collection order (group, category, priority, then name). |
| `/badge` | `title.badge` | Show your badges in chat. Click one to wear it. |
| `/badge <badge>`, `/badge none` | `title.badge` | Wear a badge by name or clear it. |
| `/shine` | `title.shine` | Show your current shine above your head. |
| `/shine <shine>`, `/shine default`, `/shine none` | `title.shine` | Select a shine by name, use the one supplied by your current title, or turn it off. |
| `/gradient -c <colors> -- <text>` | `title.gradient` | Preview a text gradient and log its component JSON. Use comma-separated hex colors, e.g. `/gradient -c FF8800,FFDD00 -- Summer Games`. |

## Admin commands

The `/titles` command and all its subcommands require `title.admin`.
Use `/titles` for in-game help. In the table below, `#category` means
a category key with a leading `#`, for example `#fishing`.

| Command | Description |
| --- | --- |
| `/titles` | Show the admin command list. |
| `/titles list` | List all titles. Click a title to inspect it. |
| `/titles search <text...>` | Find titles by internal name, display text, or description. Use lowercase search terms. |
| `/titles info <title>` | Inspect a title's appearance, description, category, priority, shine, and badge. |
| `/titles listplayers <title>` | List players with an explicit grant for this title. |
| `/titles ranktitles` | Rank titles by number of explicit grants, most common first. |
| `/titles create <title> <format...>` | Create a title or update its display text. The format accepts Minecraft color codes such as `&6`. |
| `/titles desc <title> <description...>` | Set the title's description. Alias: `description`. |
| `/titles json <title> [json]` | Set a custom display component. Omit the JSON to return to the title's formatted text. |
| `/titles namecolor <title> [color]` | Set the color or text effect applied to the wearer's name. Omit the value to clear it. |
| `/titles prefix <title> <true-or-false>` | Use `true` to display the title before the wearer's name or `false` to hide the prefix. |
| `/titles shine <title> [shine]` | Assign the title's default shine. Omit the shine to clear it. |
| `/titles priority <title> <number>` | Set the sort order within the title's category. Lower numbers come first. Alias: `prio`. |
| `/titles category <title> [category]` | Assign the title to a collection category. Omit the category to clear it. |
| `/titles setsuffix <title> [suffix]` | Give owners access to a badge or a whole badge category (`#category`). Omit the value to clear the link. |
| `/titles createmytemstitleandsuffix <mytems> <category>` | Create a prefix title and a matching badge from a Mytems icon in the given category. |
| `/titles delete <title>` | Delete the title, remove its explicit grants, and clear player selections that use it. |
| `/titles player <player>` | List a player's titles and mark the selected title in brackets when available. Offline listings use stored unlocks. |
| `/titles has <player> <title>` | Check whether a player has a title. |
| `/titles unlock <player> <title>` | Grant one title to a player. |
| `/titles unlockset <player> <title...>` | Try the titles in order, grant the first one without an explicit grant, and offer it to the player if online. |
| `/titles unlockset <player> #category` | Grant the whole title category. Titles added to that category later are included. |
| `/titles lock <player> <title>` | Revoke an explicit title grant. Use `#category` in place of the title to revoke a category grant. |
| `/titles set <player> <title>` | Make the player wear a title they already have. |
| `/titles reset <player>` | Clear the player's selection and return to their default title. |
| `/titles transfer <from> <to>` | Move individual title grants from the first player to the second. The first player loses those grants even if the second already has them. |
| `/titles suffix` | Show the badge management commands. |
| `/titles suffix list` | List badge categories and individual badges. |
| `/titles suffix info <name>` | Inspect a badge's format and appearance, or use `#category` to list badges in that category. |
| `/titles suffix create <name> <format>` | Create a badge or change how an existing badge is displayed. |
| `/titles suffix category <name> [category]` | Assign a badge to a category. Omit the category to clear it. |
| `/titles suffix player <player>` | List the badges available in the player's loaded session. |
| `/titles suffix unlock <suffix> <player>` | Grant a badge or badge category (`#category`) to a player. |
| `/titles suffix lock <suffix> <player>` | Revoke a player's explicit grant for a badge or badge category (`#category`). |
| `/titles shines` | Show the shine management commands. |
| `/titles shines show <shine> [distance amount]` | Preview a shine above your head. Supply both distance and amount to show it ahead of you a given number of times. Run as a player. |
| `/titles shines disable` | Temporarily disable player shine effects on this server. |
| `/titles shines enable` | Re-enable player shine effects on this server. |
| `/titles reload` | Reload the title and badge catalogs from SQL and reload local online players' sessions. |
| `/titles refreshplayers` | Rebuild the displayed names of all players online on this server. |
| `/titles session [player]` | Check online players for missing sessions, or inspect one player's cached selections and rendered name. |
| `/titles html` | Export the title catalog to `index.html` in the plugin's data folder. Hidden titles are excluded. |

## Creating and granting a title

These in-game commands create a gold fishing title and award it to
Alex.

```text
/titles create Angler &6[Angler]
/titles category Angler fishing
/titles prefix Angler true
/titles unlock Alex Angler
```

Alex can then wear it with `/title Angler`. New titles start in the
hidden category. Assigning the fishing category makes this one visible
in the collection book even to players who have yet to earn it. The
prefix setting makes `[Angler]` appear before the name.

For rank-based awards, grant the permission
`title.unlock.<lowercase-title-name>` (here,
`title.unlock.angler`). To award a badge separately, use `/titles
suffix unlock <badge> <player>`. To include an existing badge with a
title, use `/titles setsuffix <title> <badge>`.

## Server setup and integrations

Title targets Paper 26.3 and requires **Core**, **SQL**, and
**Mytems**. It creates its tables on startup and stores the catalog,
explicit unlocks, and player selections in SQL. Grant `title.title`,
`title.badge`, and `title.shine` to the player groups that should use
those commands. All three default to operators.

Other plugins can call `TitlePlugin.getInstance()` to grant titles or
customize a player's appearance. The `setColor`,
`setPlayerListPrefix`, and `setPlayerListSuffix` methods apply to the
current login. They can be called during join while SQL is loading and
survive title data reloads. Pass `null` to clear one setting. Quitting
clears all three, so a minigame that retains team assignments across
reconnects should reapply its team color on join.
