package net.frozenorb.potpvp.profile.setting;

import com.google.common.collect.ImmutableList;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum Setting {

    SHOW_SCOREBOARD(
        ChatColor.LIGHT_PURPLE + "Match Scoreboard",
        ImmutableList.of(
            ChatColor.BLUE + "Toggles side scoreboard in-match"
        ),
        Material.ITEM_FRAME,
        ChatColor.GREEN + "Enabled",
        ChatColor.RED + "Disabled",
        true,
        null // no permission required
    ),
    SHOW_PING(
        ChatColor.LIGHT_PURPLE + "Ping Display",
        ImmutableList.of(
            ChatColor.BLUE + "Toggles if the ping is displayed on the scoreboard"
        ),
        Material.ITEM_FRAME,
        ChatColor.GREEN + "Enabled",
        ChatColor.RED + "Disabled",
        true,
        null // no permission required
    ),
    SHOW_SPECTATOR_JOIN_MESSAGES(
        ChatColor.AQUA + "Spectator Join Messages",
        ImmutableList.of(
            ChatColor.BLUE + "Enable this to display messages as spectators join."
        ),
        Material.BONE,
        ChatColor.GREEN + "Enabled",
        ChatColor.RED + "Disabled",
        true,
        null // no permission required
    ),
    VIEW_OTHER_SPECTATORS(
        ChatColor.GREEN + "Other Spectators",
        ImmutableList.of(
            ChatColor.BLUE + "If enabled, you can see spectators",
            ChatColor.BLUE + "in the same match as you.",
            "",
            ChatColor.BLUE + "Disable to only see alive players in match."
        ),
        Material.GLASS_BOTTLE,
        ChatColor.GREEN + "Show spectators",
        ChatColor.RED + "Hide spectators",
        true,
        null // no permission required
    ),
    ALLOW_SPECTATORS(
            ChatColor.DARK_GREEN + "Allow Spectators",
            ImmutableList.of(
                    ChatColor.BLUE + "If enabled, players can spectate your",
                    ChatColor.BLUE + "matches with /spectate.",
                    "",
                    ChatColor.BLUE + "Disable to disallow match spectators."
            ),
            Material.REDSTONE_TORCH_ON,
            ChatColor.GREEN + "Enabled",
            ChatColor.RED + "Disabled",
            true,
            null // no permission required
    ),
    RECEIVE_DUELS(
        ChatColor.GREEN + "Duel Invites",
        ImmutableList.of(
            ChatColor.BLUE + "If enabled, you will be able to receive",
            ChatColor.BLUE + "duels from other players or parties.",
           "",
            ChatColor.BLUE + "Disable to not receive, but still send duels."
        ),
        Material.IRON_SWORD,
        ChatColor.GREEN + "Enabled",
        ChatColor.RED + "Disabled",
        true,
        null
    ),
    VIEW_OTHERS_LIGHTNING(
        ChatColor.GREEN + "Death Lightning",
        ImmutableList.of(
            ChatColor.BLUE + "If enabled, lightning will be visible",
            ChatColor.BLUE + "when other players die.",
            "",
            ChatColor.BLUE + "Disable to hide others lightning."
        ),
        Material.TORCH,
        ChatColor.GREEN + "Show lightning",
        ChatColor.RED + "Hide lightning",
        true,
        null
    ),
    NIGHT_MODE(
        ChatColor.GRAY + "Night Mode",
        ImmutableList.of(
            ChatColor.BLUE + "If enabled, your player time will be",
            ChatColor.BLUE + "changed to night time.",
            "",
            ChatColor.BLUE + "Disable to play in day time."
        ),
        Material.GLOWSTONE,
        ChatColor.GREEN + "Enabled",
        ChatColor.RED + "Disabled",
        false,
        null // no permission required
    ),
    PLAYERS_IN_LOBBY(
        ChatColor.RED + "Players in Lobby",
        ImmutableList.of(
            ChatColor.BLUE + "If enabled, you will see",
            ChatColor.BLUE + "ranked players in the lobby.",
            "",
            ChatColor.BLUE + "Disable to see nobody."
        ),
        Material.EYE_OF_ENDER,
        ChatColor.GREEN + "Enabled",
        ChatColor.RED + "Disabled",
        false,
        "potpvp.donator" // no permission required
    ),
    FLY_IN_LOBBY(
        ChatColor.RED + "Fly in Lobby",
        ImmutableList.of(
            ChatColor.BLUE + "If enabled, you will be",
            ChatColor.BLUE + "able to fly in the lobby.",
            "",
            ChatColor.BLUE + "Disable to not fly."
        ),
        Material.FEATHER,
        ChatColor.GREEN + "Enabled",
        ChatColor.RED + "Disabled",
        false,
        "potpvp.donator" // no permission required
    ),
    SEE_TOURNAMENT_JOIN_MESSAGE(
            ChatColor.DARK_PURPLE + "Tournament Join Messages",
            ImmutableList.of(
                ChatColor.BLUE + "If enabled, you will see messages",
                ChatColor.BLUE + "when people join the tournament",
                "",
                ChatColor.BLUE + "Disable to only see your own party join messages."
            ),
            Material.IRON_DOOR,
            ChatColor.GREEN + "Enabled",
            ChatColor.RED + "Disabled",
            true,
            null // no permission required
    ),
    SEE_TOURNAMENT_ELIMINATION_MESSAGES(
            ChatColor.DARK_PURPLE + "Tournament Elimination Messages",
            ImmutableList.of(
                ChatColor.BLUE + "If enabled, you will see messages when",
                ChatColor.BLUE + "people are eliminated the tournament",
                "",
                ChatColor.BLUE + "Disable to only see your own party elimination messages."
            ),
            Material.SKULL_ITEM,
            ChatColor.GREEN + "Enabled",
            ChatColor.RED + "Disabled",
            true,
            null // no permission required
    );

    /**
     * Friendly (colored) display name for this setting
     */
    @Getter private final String name;

    /**
     * Friendly (colored) description for this setting
     */
    @Getter private final List<String> description;

    /**
     * Material to be used when rendering an icon for this setting
     */
    @Getter private final Material icon;

    /**
     * Text to be shown when rendering an icon for this setting, while enabled
     */
    @Getter private final String enabledText;

    /**
     * Text to be shown when rendering an icon for this setting, while enabled
     */
    @Getter private final String disabledText;

    /**
     * Default value for this setting, will be used for players who haven't
     * updated the setting and if a player's settings fail to load.
     */
    private final boolean defaultValue;

    /**
     * The permission required to be able to see and update this setting,
     * null means no permission is required to update/see.
     */
    private final String permission;

    // Using @Getter means the method would be 'isDefaultValue',
    // which doesn't correctly represent this variable.
    public boolean getDefaultValue() {
        return defaultValue;
    }

    public boolean canUpdate(Player player) {
        return permission == null || player.hasPermission(permission);
    }

}