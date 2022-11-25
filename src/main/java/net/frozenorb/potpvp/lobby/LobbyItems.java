package net.frozenorb.potpvp.lobby;

import net.frozenorb.potpvp.util.ItemUtils;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import lombok.experimental.UtilityClass;

import static net.frozenorb.potpvp.PotPvPLang.LEFT_ARROW;
import static net.frozenorb.potpvp.PotPvPLang.RIGHT_ARROW;
import static org.bukkit.ChatColor.*;

import org.bukkit.ChatColor;

@UtilityClass
public final class LobbyItems {

    public static final ItemStack SPECTATE_RANDOM_ITEM = new ItemStack(Material.COMPASS);
    public static final ItemStack SPECTATE_MENU_ITEM = new ItemStack(Material.PAPER);
    public static final ItemStack ENABLE_SPEC_MODE_ITEM = new ItemStack(Material.REDSTONE_TORCH_ON);
    public static final ItemStack DISABLE_SPEC_MODE_ITEM = new ItemStack(Material.LEVER);
    public static final ItemStack MANAGE_ITEM = new ItemStack(Material.ANVIL);
    public static final ItemStack UNFOLLOW_ITEM = new ItemStack(Material.INK_SACK, 1, DyeColor.RED.getDyeData());
    public static final ItemStack PLAYER_STATISTICS = new ItemStack(Material.EMERALD);
    public static final ItemStack PARTY_ITEM = new ItemStack(Material.NAME_TAG);

    static {
        ItemUtils.setDisplayName(SPECTATE_RANDOM_ITEM,  AQUA + "Spectate Random Match");
        ItemUtils.setDisplayName(SPECTATE_MENU_ITEM,  AQUA + "Spectate Menu");
        ItemUtils.setDisplayName(DISABLE_SPEC_MODE_ITEM, RED + "Leave Spectator Mode");
        ItemUtils.setDisplayName(ENABLE_SPEC_MODE_ITEM, AQUA + "Enable Spectator Mode");
        ItemUtils.setDisplayName(MANAGE_ITEM, RED + "Manage PotPvP");
        ItemUtils.setDisplayName(UNFOLLOW_ITEM, RED + "Stop Following");
        ItemUtils.setDisplayName(PLAYER_STATISTICS, AQUA + "Statistics");
        ItemUtils.setDisplayName(PARTY_ITEM, AQUA + "Create Party");
    }

}