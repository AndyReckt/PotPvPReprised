package net.frozenorb.potpvp.lobby.menu;

import java.util.HashMap;
import java.util.Map;

import net.frozenorb.potpvp.util.ItemBuilder;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import net.frozenorb.potpvp.kit.kittype.KitType;
import net.frozenorb.potpvp.lobby.menu.statistics.GlobalEloButton;
import net.frozenorb.potpvp.lobby.menu.statistics.KitButton;
import net.frozenorb.potpvp.lobby.menu.statistics.PlayerButton;
import net.frozenorb.potpvp.util.menu.Button;
import net.frozenorb.potpvp.util.menu.Menu;
import org.jetbrains.annotations.NotNull;

public final class StatisticsMenu extends Menu {

    public StatisticsMenu() {
        setAutoUpdate(true);
        setPlaceholder(true);
    }

    @Override
    public String getTitle(Player player) {
        return "Statistics";
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        buttons.put(getSlot(3, 1), new PlayerButton());
        buttons.put(getSlot(5, 1), new GlobalEloButton());

        int y = 3;
        int x = 1;

        for (KitType kitType : KitType.getAllTypes()) {
            if (!kitType.isSupportsRanked()) continue;

            buttons.put(getSlot(x++, y), new KitButton(kitType));

            if (x == 8) {
                y++;
                x = 1;
            }
        }

        return buttons;
    }

    @Override
    public int size(Player player) {
        return 9 * 6;
    }

}