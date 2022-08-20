package net.frozenorb.potpvp.profile.setting.menu;

import net.frozenorb.potpvp.profile.setting.Setting;
import net.frozenorb.potpvp.util.ItemBuilder;
import net.frozenorb.potpvp.util.menu.Button;
import net.frozenorb.potpvp.util.menu.Menu;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * Menu used by /settings to let players toggle settings
 */
public final class SettingsMenu extends Menu {

    private static final Button BLACK_PANE = Button.fromItem(ItemBuilder.of(Material.STAINED_GLASS_PANE).data((short) 15).name(" ").build());
    int i, y;

    public SettingsMenu() {
        setAutoUpdate(true);
        i = 1;
        y = 1;
    }

    @Override
    public String getTitle(Player player) {
        return "Edit settings";
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

//        for (int x = 0; x < (9 * 4); x++) {
//            buttons.put(x, BLACK_PANE);
//        }
        int x = 0;
        for (Setting setting : Setting.values()) {
            buttons.put(x++, new SettingButton(setting));
        }

//        for (Setting setting : Setting.values()) {
//            if (setting.canUpdate(player)) {
//                buttons.put(getSlot(i, y), new SettingButton(setting));
//                i++;
//                if (i == 8) {
//                    y++;
//                    i = 1;
//                }
//            }
//        }
        return buttons;
    }
}