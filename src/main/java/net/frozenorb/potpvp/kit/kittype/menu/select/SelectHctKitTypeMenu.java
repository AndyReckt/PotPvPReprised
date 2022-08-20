package net.frozenorb.potpvp.kit.kittype.menu.select;

import com.google.common.base.Preconditions;
import net.frozenorb.potpvp.PotPvPRP;
import net.frozenorb.potpvp.kit.kittype.KitType;
import net.frozenorb.potpvp.party.Party;
import net.frozenorb.potpvp.util.Callback;
import net.frozenorb.potpvp.util.InventoryUtils;
import net.frozenorb.potpvp.util.menu.Button;
import net.frozenorb.potpvp.util.menu.Menu;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public final class SelectHctKitTypeMenu extends Menu {

    private final boolean reset;
    private final String title;
    private final Callback<KitType> callback;

    public SelectHctKitTypeMenu(Callback<KitType> callback, String title) {
        this(callback, true, title);
        setPlaceholder(true);
    }

    public SelectHctKitTypeMenu(Callback<KitType> callback, boolean reset, String title) {
        this.callback = Preconditions.checkNotNull(callback, "callback");
        this.reset = reset;
        this.title = title;
    }
    

    @Override
    public void onClose(Player player) {
        if (reset) {
            InventoryUtils.resetInventoryDelayed(player);
        }
    }

    @Override
    public String getTitle(Player player) {
        return ChatColor.BLUE.toString() + ChatColor.BOLD + title;
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        buttons.put(3, new HctKitTypeButton(callback, "BARD_HCF"));
        buttons.put(4, new HctKitTypeButton(callback, "DIAMOND_HCF"));
        buttons.put(5, new HctKitTypeButton(callback, "ARCHER_HCF"));
        return buttons;
    }

}