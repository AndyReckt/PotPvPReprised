package net.frozenorb.potpvp.kit.kittype.menu.select;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import net.frozenorb.potpvp.kit.kittype.KitType;
import net.frozenorb.potpvp.util.Callback;
import net.frozenorb.potpvp.util.menu.Button;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.ArrayList;
import java.util.List;

final class HctKitTypeButton extends Button {

    private final KitType kitType;
    private final Callback<KitType> callback;
    private final List<String> descriptionLines;
    private final int amount;

    HctKitTypeButton(Callback<KitType> callback, String id) {
        this(KitType.byId(id),callback, ImmutableList.of(), 1);
    }

    HctKitTypeButton(KitType kitType, Callback<KitType> callback, List<String> descriptionLines, int amount) {
        this.kitType = Preconditions.checkNotNull(kitType, "kitType");
        this.callback = Preconditions.checkNotNull(callback, "callback");
        this.descriptionLines = ImmutableList.copyOf(descriptionLines);
        this.amount = amount;
    }

    @Override
    public String getName(Player player) {
        return ChatColor.RED + "HCF Kits";
    }

    @Override
    public List<String> getDescription(Player player) {
        List<String> description = new ArrayList<>();

        if (!descriptionLines.isEmpty()) {
            description.addAll(descriptionLines);
        }

        description.add("");
        description.add(ChatColor.YELLOW + "Click here to select " + ChatColor.YELLOW + ChatColor.BOLD + kitType.getDisplayName() + ChatColor.YELLOW + ".");

        return description;
    }

    @Override
    public Material getMaterial(Player player) {
        return kitType.getIcon().getItemType();
    }

    @Override
    public int getAmount(Player player) {
        return amount;
    }

    @Override
    public byte getDamageValue(Player player) {
        return kitType.getIcon().getData();
    }

    @Override
    public void clicked(Player player, int slot, ClickType clickType) {
        callback.callback(kitType);
    }

}