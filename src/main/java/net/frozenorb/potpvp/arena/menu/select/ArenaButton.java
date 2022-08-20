package net.frozenorb.potpvp.arena.menu.select;

import java.util.List;
import java.util.Set;

import net.frozenorb.potpvp.arena.ArenaSchematic;
import net.frozenorb.potpvp.util.Callback;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import com.google.common.collect.Lists;

import lombok.AllArgsConstructor;
import net.frozenorb.potpvp.util.menu.Button;
import org.bukkit.inventory.InventoryView;

@AllArgsConstructor
public class ArenaButton extends Button {

    private ArenaSchematic schematic;
    private Callback<ArenaSchematic> callback;

    @Override
    public String getName(Player player) {
        return ChatColor.AQUA + schematic.getName();
    }
    
    @Override
    public List<String> getDescription(Player player) {
        List<String> lines = Lists.newLinkedList();

        lines.add(" ");
        lines.add(ChatColor.GRAY.toString() + ChatColor.ITALIC + "Click here to select this arena.");
        
        return lines;
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
        callback.callback(schematic);
    }
}
