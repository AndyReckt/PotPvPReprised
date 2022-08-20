package net.frozenorb.potpvp.match.listener;

import lombok.Getter;
import net.frozenorb.potpvp.PotPvPRP;
import net.frozenorb.potpvp.match.Match;
import net.frozenorb.potpvp.match.MatchTeam;
import net.frozenorb.potpvp.match.event.MatchEndEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;

import java.util.HashMap;
import java.util.UUID;

public class MatchBoxingListener implements Listener {

    @Getter
    static HashMap<UUID, Integer> hitMap = new HashMap<>();
    @Getter
    static HashMap<MatchTeam, Integer> hitMapTeam = new HashMap<>();

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if(event.getDamager() instanceof Player) {
                Player attacker = (Player) event.getDamager();
                Match match = PotPvPRP.getInstance().getMatchHandler().getMatchPlaying(attacker);
                if (match != null) {
                    if (match.getKitType().isBoxfight()){
                        if (match.getTeams().size() == 2) {
                            MatchTeam team1 = match.getTeams().get(0);
                            MatchTeam team2 = match.getTeams().get(1);
                            if(team1.getAliveMembers().size() == 1 && team2.getAliveMembers().size() == 1) {
                                hitMap.put(attacker.getUniqueId(), hitMap.getOrDefault(attacker.getUniqueId(), 0) + 1);
                                if(hitMap.get(attacker.getUniqueId()) >= 100) {
                                    player.setHealth(0);
                                    hitMap.remove(player.getUniqueId());
                                    hitMap.remove(attacker.getUniqueId());
                                }
                            } else {
                                MatchTeam attackerTeam = match.getTeam(attacker.getUniqueId());
                                hitMapTeam.put(attackerTeam, hitMapTeam.getOrDefault(attackerTeam, 0) + 1);

                                if(hitMapTeam.get(attackerTeam) >= (100 * attackerTeam.getAliveMembers().size())) {
                                    match.getTeam(player.getUniqueId()).getAliveMembers().forEach(m ->{
                                        player.setHealth(0);
                                    });
                                    hitMapTeam.remove(attackerTeam);
                                    hitMapTeam.remove(match.getTeam(player.getUniqueId()));
                                }

                            }
                        } else {
                            hitMap.put(attacker.getUniqueId(), hitMap.getOrDefault(attacker.getUniqueId(), 0) + 1);
                            if(hitMap.get(attacker.getUniqueId()) >= 100) {
                                match.getTeams().forEach(matchTeam -> matchTeam.getAllMembers().forEach(member -> {
                                   if(!(member == attacker.getUniqueId())) {
                                       Player play = Bukkit.getPlayer(member);
                                       play.setHealth(0);
                                       hitMap.remove(member);
                                   }
                                }));
                                hitMap.remove(attacker.getUniqueId());
                            }
                        }
                        event.setDamage(0.0);
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMatchEnd(MatchEndEvent event) {
        Match match = event.getMatch();

        if(match.getKitType().isBoxfight()) {
            for(UUID uuid : match.getAllPlayers()) {
                hitMap.remove(uuid);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHungerLoss(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player=(Player) event.getEntity();
            Match match=PotPvPRP.getInstance().getMatchHandler().getMatchPlaying(player);
            if (match.getKitType().isBoxfight()) {
                event.setCancelled(true);
                player.setSaturation(10.0F);
            }
        }
    }

}