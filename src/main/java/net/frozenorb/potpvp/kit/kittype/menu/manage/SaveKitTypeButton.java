package net.frozenorb.potpvp.kit.kittype.menu.manage;

import com.google.common.collect.ImmutableList;

import net.frozenorb.potpvp.kit.kittype.KitType;
import net.frozenorb.potpvp.util.menu.Button;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.List;

final class SaveKitTypeButton extends Button {

    private final KitType type;

    SaveKitTypeButton(KitType type) {
        this.type = type;
    }

    @Override
    public String getName(Player player) {
        return ChatColor.GREEN.toString() + ChatColor.BOLD + "Save";
    }

    @Override
    public List<String> getDescription(Player player) {
        return ImmutableList.of(
            "",
            ChatColor.YELLOW + "Click this to save the kit editor items."
        );
    }

    @Override
    public Material getMaterial(Player player) {
        return Material.WOOL;
    }

    @Override
    public byte getDamageValue(Player player) {
        return DyeColor.LIME.getWoolData();
    }

    @Override
    public void clicked(Player player, int slot, ClickType clickType) {
        ItemStack[] fullInv = player.getInventory().getContents();
        ItemStack[] kitInventory = new ItemStack[28];
        int index = 0;

        for (int i = 9; i < 36; i++) {
            ItemStack item = fullInv[i];
            if (item != null) {
                kitInventory[index++] = item;
            }
        }

        type.setEditorItems(kitInventory);
        type.saveAsync();

        player.closeInventory();
        player.sendMessage(ChatColor.GREEN + "Kit editor items saved.");
    }
}
