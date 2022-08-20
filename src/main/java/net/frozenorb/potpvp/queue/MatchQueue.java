package net.frozenorb.potpvp.queue;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import net.frozenorb.potpvp.PotPvPRP;
import net.frozenorb.potpvp.profile.elo.EloHandler;
import net.frozenorb.potpvp.kit.kittype.KitType;
import net.frozenorb.potpvp.match.Match;
import net.frozenorb.potpvp.match.MatchHandler;
import net.frozenorb.potpvp.match.MatchTeam;
import net.frozenorb.potpvp.util.PatchedPlayerUtils;

import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import lombok.Getter;

public final class MatchQueue {

    @Getter private final KitType kitType;
    @Getter private final boolean ranked;
    private final List<MatchQueueEntry> entries = new CopyOnWriteArrayList<>();

    MatchQueue(KitType kitType, boolean ranked) {
        this.kitType = Preconditions.checkNotNull(kitType, "kitType");
        this.ranked = ranked;
    }

    void tick() {
        // we clone so we can remove entries from our working set
        // (sometimes matches fail to create [ex no maps open] and
        // we should retry)
        List<MatchQueueEntry> entriesCopy = new ArrayList<>(entries);
        EloHandler eloHandler = PotPvPRP.getInstance().getEloHandler();

        // ranked match algorithm requires entries are in
        // order by elo. There's no reason we only do this for ranked
        // matches aside from performance
        if (ranked) {
            entriesCopy.sort(Comparator.comparing(e -> eloHandler.getElo(e.getMembers(), kitType)));
        }

        while (entriesCopy.size() >= 2) {
            // remove from 0 both times because index shifts down
            MatchQueueEntry a = entriesCopy.remove(0);
            MatchQueueEntry b = entriesCopy.remove(0);

            // the algorithm for ranked and unranked queues is actually very similar,
            // except for the fact ranked matches can't be made if the elo window for
            // both players don't overlap
            if (ranked) {
                int aElo = eloHandler.getElo(a.getMembers(), kitType);
                int bElo = eloHandler.getElo(b.getMembers(), kitType);

                int aEloWindow = a.getWaitSeconds() * QueueHandler.RANKED_WINDOW_GROWTH_PER_SECOND;
                int bEloWindow = b.getWaitSeconds() * QueueHandler.RANKED_WINDOW_GROWTH_PER_SECOND;

                if (Math.abs(aElo - bElo) > Math.max(aEloWindow, bEloWindow)) {
                    continue;
                }
            }

            createMatchAndRemoveEntries(a, b);
        }
    }

    public int countPlayersQueued() {
        int count = 0;

        for (MatchQueueEntry entry : entries) {
            count += entry.getMembers().size();
        }

        return count;
    }

    void addToQueue(MatchQueueEntry entry) {
        entries.add(entry);
    }

    void removeFromQueue(MatchQueueEntry entry) {
        entries.remove(entry);
    }

    private void createMatchAndRemoveEntries(MatchQueueEntry entryA, MatchQueueEntry entryB) {
        MatchHandler matchHandler = PotPvPRP.getInstance().getMatchHandler();
        QueueHandler queueHandler = PotPvPRP.getInstance().getQueueHandler();

        MatchTeam teamA = new MatchTeam(entryA.getMembers());
        MatchTeam teamB = new MatchTeam(entryB.getMembers());

        Match match = matchHandler.startMatch(
            ImmutableList.of(teamA, teamB),
            kitType,
            ranked,
            !ranked // allowRematches is the inverse of ranked
        );

        // only remove entries if match creation was successfull
        if (match != null) {
            queueHandler.removeFromQueueCache(entryA);
            queueHandler.removeFromQueueCache(entryB);

            String teamAElo = "";
            String teamBElo = "";

            if (ranked) {
                EloHandler eloHandler = PotPvPRP.getInstance().getEloHandler();
                int eloA = eloHandler.getElo(teamA.getAliveMembers(), kitType);
                int eloB = eloHandler.getElo(teamB.getAliveMembers(), kitType);


                teamAElo = " " + eloA + " Elo " + eloDiff(eloA, eloB);
                teamBElo = " " + eloB + " Elo " + eloDiff(eloB, eloA);
            }

            StringBuilder sb = new StringBuilder("\n");
            sb.append(ChatColor.LIGHT_PURPLE).append("Match found!");
            sb.append("\n\n");
            sb.append(ChatColor.WHITE).append("Kit: ").append(ChatColor.AQUA).append(kitType.getDisplayName());
            sb.append("\n");
            sb.append(ChatColor.WHITE).append("Arena: ").append(ChatColor.AQUA).append(match.getArena().getSchematic());
            sb.append("\n");
            teamA.messageAlive(sb.toString());
            teamB.messageAlive(sb.toString());

            if (ranked) {
                teamB.messageAlive(ChatColor.WHITE + "Elo: " + ChatColor.AQUA + teamAElo + "\n");
                teamA.messageAlive(ChatColor.WHITE + "Elo: " + ChatColor.AQUA + teamBElo + "\n");
            }
            match.messageAlive("\n");

            entries.remove(entryA);
            entries.remove(entryB);
        }
    }

    private String eloDiff(int elo1, int elo2) {
        if (elo1 > elo2) return ChatColor.GREEN + "(+" + (elo1 - elo2) + ")";
        else return ChatColor.RED + "(-" + (elo2 - elo1) + ")";
    }

}