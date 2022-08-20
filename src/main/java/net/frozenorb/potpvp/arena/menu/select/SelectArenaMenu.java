package net.frozenorb.potpvp.arena.menu.select;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.frozenorb.potpvp.arena.Arena;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.frozenorb.potpvp.PotPvPRP;
import net.frozenorb.potpvp.arena.ArenaSchematic;
import net.frozenorb.potpvp.kit.kittype.KitType;
import net.frozenorb.potpvp.kit.kittype.menu.select.SendDuelButton;
import net.frozenorb.potpvp.kit.kittype.menu.select.ToggleAllButton;
import net.frozenorb.potpvp.match.MatchHandler;
import net.frozenorb.potpvp.util.menu.Button;
import net.frozenorb.potpvp.util.menu.Menu;
import net.frozenorb.potpvp.util.Callback;

public class SelectArenaMenu extends Menu {
    
    private KitType kitType;
    private Callback<ArenaSchematic> arenaCallback;
    private String title;
    Set<ArenaSchematic> allMaps = new HashSet<>();

    public SelectArenaMenu(KitType kitType, Callback<ArenaSchematic> arenaCallback, String title) {
        this.kitType = kitType;
        this.arenaCallback = arenaCallback;
        this.title = title;
        
        for (ArenaSchematic schematic : PotPvPRP.getInstance().getArenaHandler().getSchematics()) {
            if (MatchHandler.canUseSchematic(this.kitType, schematic)) {
                allMaps.add(schematic);
            }
        }
    }

    @Override
    public String getTitle(Player player) {
        return ChatColor.BLUE.toString() + ChatColor.BOLD + title;
    }

    @Override
    public Map<Integer, Button> getButtons(Player arg0) {
        Map<Integer, Button> buttons = Maps.newHashMap();

        int i = 0;
        for (ArenaSchematic schematic : allMaps) {
            buttons.put(i++, new ArenaButton(schematic, arenaCallback));
        }
        
        return buttons;
    }
    
}
