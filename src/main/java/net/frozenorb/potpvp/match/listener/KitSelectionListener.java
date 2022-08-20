package net.frozenorb.potpvp.match.listener;

import me.andyreckt.holiday.utils.CC;
import net.frozenorb.potpvp.PotPvPRP;
import net.frozenorb.potpvp.kit.Kit;
import net.frozenorb.potpvp.kit.KitHandler;
import net.frozenorb.potpvp.kit.kittype.KitType;
import net.frozenorb.potpvp.match.Match;
import net.frozenorb.potpvp.match.MatchHandler;
import net.frozenorb.potpvp.match.MatchTeam;
import net.frozenorb.potpvp.match.event.MatchCountdownStartEvent;

import net.frozenorb.potpvp.match.event.MatchEndEvent;
import net.frozenorb.potpvp.party.Party;
import net.frozenorb.potpvp.pvpclasses.PvPClasses;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class KitSelectionListener implements Listener {

    public static ArrayList<UUID> kitTaken = new ArrayList<>();


    /**
     * Give players their kits when their match countdown starts
     */
    @EventHandler
    public void onMatchCountdownStart(MatchCountdownStartEvent event) {
        KitHandler kitHandler = PotPvPRP.getInstance().getKitHandler();
        Match match = event.getMatch();
        KitType kitType = match.getKitType();

        if (kitType.isSumo()) return; // no kits for sumo

        for (Player player : Bukkit.getOnlinePlayers()) {
            MatchTeam team = match.getTeam(player.getUniqueId());

            if (team == null) {
                continue;
            }

            List<Kit> customKits = kitHandler.getKits(player, kitType);
            ItemStack defaultKitItem = Kit.ofDefaultKit(kitType).createSelectionItem();

            if (kitType.equals(KitType.teamFight)) {
                KitType bard = KitType.byId("BARD_HCF");
                KitType diamond = KitType.byId("DIAMOND_HCF");
                KitType archer = KitType.byId("ARCHER_HCF");

                Party party = PotPvPRP.getInstance().getPartyHandler().getParty(player);

                if (party == null) {
                    Kit.ofDefaultKit(diamond).apply(player);
                } else {
                    PvPClasses kit = party.getKits().getOrDefault(player.getUniqueId(), PvPClasses.DIAMOND);

                    if (kit == null || kit == PvPClasses.DIAMOND) {
                        Kit.ofDefaultKit(diamond).apply(player);
                    } else if (kit == PvPClasses.BARD) {
                        Kit.ofDefaultKit(bard).apply(player);
                    } else {
                        Kit.ofDefaultKit(archer).apply(player);
                    }

                }

            } else {
                // if they have no kits saved place default in 0, otherwise
                // the default goes in 9 and they get custom kits from 1-4
                if (customKits.isEmpty()) {
                    player.getInventory().setItem(0, defaultKitItem);
                } else {
                    for (Kit customKit : customKits) {
                        // subtract one to convert from 1-indexed kts to 0-indexed inventories
                        player.getInventory().setItem(customKit.getSlot() - 1, customKit.createSelectionItem());
                    }

                    player.getInventory().setItem(8, defaultKitItem);
                }
            }


            player.updateInventory();
        }
    }

    /**
     * Don't let players drop their kit selection books via the Q key
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        MatchHandler matchHandler = PotPvPRP.getInstance().getMatchHandler();
        Match match = matchHandler.getMatchPlaying(event.getPlayer());

        if (match == null) {
            return;
        }

        KitHandler kitHandler = PotPvPRP.getInstance().getKitHandler();
        ItemStack droppedItem = event.getItemDrop().getItemStack();
        KitType kitType = match.getKitType();

        for (Kit kit : kitHandler.getKits(event.getPlayer(), kitType)) {
            if (kit.isSelectionItem(droppedItem)) {
                event.setCancelled(true);
                return;
            }
        }

        Kit defaultKit = Kit.ofDefaultKit(kitType);

        if (defaultKit.isSelectionItem(droppedItem)) {
            event.setCancelled(true);
        }
    }

    /**
     * Don't let players drop their kit selection items via death
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        MatchHandler matchHandler = PotPvPRP.getInstance().getMatchHandler();
        Match match = matchHandler.getMatchPlaying(event.getEntity());

        if (match == null) {
            return;
        }

        KitHandler kitHandler = PotPvPRP.getInstance().getKitHandler();
        KitType kitType = match.getKitType();

        for (Kit kit : kitHandler.getKits(event.getEntity(), kitType)) {
            event.getDrops().remove(kit.createSelectionItem());
        }

        event.getDrops().remove(Kit.ofDefaultKit(kitType).createSelectionItem());
    }

    /**
     * Give players their kits upon right click
     */
    // no ignoreCancelled = true because right click on air
    // events are by default cancelled (wtf Bukkit)
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.hasItem() || !event.getAction().name().contains("RIGHT_")) {
            return;
        }

        MatchHandler matchHandler = PotPvPRP.getInstance().getMatchHandler();
        Match match = matchHandler.getMatchPlaying(event.getPlayer());

        if (match == null) {
            return;
        }

        KitHandler kitHandler = PotPvPRP.getInstance().getKitHandler();
        ItemStack clickedItem = event.getItem();
        KitType kitType = match.getKitType();
        Player player = event.getPlayer();

        for (Kit kit : kitHandler.getKits(player, kitType)) {
            if (!kit.isSelectionItem(clickedItem)) continue;
            if (kitTaken.contains(player.getUniqueId())) {
                player.sendMessage(CC.RED + "You cannot take more than one kit per match");
                return;
            }
            kit.apply(player);
            player.sendMessage(ChatColor.YELLOW + "You equipped your \"" + kit.getName() + "\" " + kitType.getDisplayName() + " kit.");
            kitTaken.add(player.getUniqueId());
            match.getUsedKit().put(player.getUniqueId(), kit);
            return;
        }

        Kit defaultKit = Kit.ofDefaultKit(kitType);

        if (defaultKit.isSelectionItem(clickedItem)) {
            if (kitTaken.contains(player.getUniqueId())) {
                player.sendMessage(CC.RED + "You cannot take more than one kit per match");
                return;
            }
            defaultKit.apply(player);
            player.sendMessage(ChatColor.YELLOW + "You equipped the default kit for " + kitType.getDisplayName() + ".");
            kitTaken.add(player.getUniqueId());
            match.getUsedKit().put(player.getUniqueId(), defaultKit);
        }
    }

    @EventHandler
    public void onMatchEndEvent(MatchEndEvent event) {
        Match match = event.getMatch();
        match.getAllPlayers().forEach(player -> kitTaken.remove(player));
    }

}