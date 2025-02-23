package net.frozenorb.potpvp.kit.kittype.menu.select;

import com.google.common.base.Preconditions;

import net.frozenorb.potpvp.PotPvPRP;
import net.frozenorb.potpvp.kit.kittype.KitType;
import net.frozenorb.potpvp.util.Callback;
import net.frozenorb.potpvp.party.Party;
import net.frozenorb.potpvp.util.InventoryUtils;
import net.frozenorb.potpvp.util.menu.Button;
import net.frozenorb.potpvp.util.menu.Menu;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.*;

public final class SelectKitTypeMenu extends Menu {

    private final boolean reset;
    private final String title;
    private final Callback<KitType> callback;
    private boolean editor = false;

    public SelectKitTypeMenu(Callback<KitType> callback, String title) {
        this(callback, true, title);
        setPlaceholder(true);
    }
    public SelectKitTypeMenu(boolean editor, Callback<KitType> callback, String title) {
        this(callback, title);
        this.editor = editor;
    }

    public SelectKitTypeMenu(Callback<KitType> callback, boolean reset, String title) {
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
        int index = 0;

        for (KitType kitType : KitType.getAllTypes()) {
            if (!player.isOp() && kitType.isHidden()) {
                continue;
            }

            if (editor && !kitType.isEditable()) {
                continue;
            }

            buttons.put(index++, new KitTypeButton(kitType, callback));
        }

        Party party = PotPvPRP.getInstance().getPartyHandler().getParty(player);
        if (party != null) {
            buttons.put(index, new KitTypeButton(KitType.teamFight, callback));
        }

        return buttons;
    }

}