package net.frozenorb.potpvp;

import lombok.Getter;
import me.andyreckt.holiday.Holiday;
import me.andyreckt.holiday.player.Profile;
import org.bukkit.Bukkit;

import java.util.stream.Collectors;

@Getter
public class PotPvPCache implements Runnable {

    private int onlineCount = 0;
    private int fightsCount = 0;
    private int queuesCount = 0;

    @Override
    public void run() {
        onlineCount = (int) Holiday.getInstance().getProfileHandler().getOnlineProfiles().stream().filter(profile -> !profile.isVanished()).count();
        fightsCount = PotPvPRP.getInstance().getMatchHandler().countPlayersPlayingInProgressMatches();
        queuesCount = PotPvPRP.getInstance().getQueueHandler().getQueuedCount();
    }

}
