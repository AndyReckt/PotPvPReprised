package net.frozenorb.potpvp.arena.menu.manageschematics;

import com.google.common.base.Preconditions;

import net.frozenorb.potpvp.PotPvPRP;
import net.frozenorb.potpvp.arena.Arena;
import net.frozenorb.potpvp.arena.ArenaHandler;
import net.frozenorb.potpvp.arena.ArenaSchematic;
import net.frozenorb.potpvp.arena.menu.manageschematic.ManageSchematicMenu;
import net.frozenorb.potpvp.util.menu.Button;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.InventoryView;

import java.util.ArrayList;
import java.util.List;

final class ManageSchematicButton extends Button {

    private final ArenaSchematic schematic;

    ManageSchematicButton(ArenaSchematic schematic) {
        this.schematic = Preconditions.checkNotNull(schematic, "schematic");
    }

    @Override
    public String getName(Player player) {
        return (schematic.isEnabled() ? ChatColor.GREEN : ChatColor.RED) + "Manage " + schematic.getName();
    }

    @Override
    public List<String> getDescription(Player player) {
        ArenaHandler arenaHandler = PotPvPRP.getInstance().getArenaHandler();
        int totalCopies = 0;
        int inUseCopies = 0;

        for (Arena arena : arenaHandler.getArenas(schematic)) {
            totalCopies++;

            if (arena.isInUse()) {
                inUseCopies++;
            }
        }

        List<String> description = new ArrayList<>();

        description.add("");
        description.add(ChatColor.GREEN + "Enabled: " + ChatColor.WHITE + (schematic.isEnabled() ? "Yes" : "No"));
        description.add(ChatColor.GREEN + "Copies: " + ChatColor.WHITE + totalCopies);
        description.add(ChatColor.GREEN + "Copies in use: " + ChatColor.WHITE + inUseCopies);

        return description;
    }

    @Override
    public int getAmount(Player player) {
        ArenaHandler arenaHandler = PotPvPRP.getInstance().getArenaHandler();
        return arenaHandler.getArenas(schematic).size();
    }

    @Override
    public Material getMaterial(Player player) {
        return schematic.getIcon().getItemType();
    }

    @Override
    public byte getDamageValue(Player player) {
        return schematic.getIcon().getData();
    }

    @Override
    public void clicked(Player player, int slot, ClickType clickType) {
        player.closeInventory();
        new ManageSchematicMenu(schematic).openMenu(player);
    }

}