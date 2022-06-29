package net.frozenorb.potpvp.adapter.nametag;

import me.andyreckt.holiday.Holiday;
import net.frozenorb.potpvp.PotPvPRP;
import net.frozenorb.potpvp.match.Match;
import net.frozenorb.potpvp.match.MatchHandler;
import net.frozenorb.potpvp.match.MatchTeam;
import net.frozenorb.potpvp.profile.follow.FollowHandler;
import net.frozenorb.potpvp.pvpclasses.pvpclasses.ArcherClass;
import net.frozenorb.potpvp.util.nametags.BufferedNametag;
import net.frozenorb.potpvp.util.nametags.OstentusAdapter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class NameTagAdapter implements OstentusAdapter {

    public static ChatColor getNameColor(Player toRefresh, Player refreshFor) {
        MatchHandler matchHandler = PotPvPRP.getInstance().getMatchHandler();

        if (matchHandler.isPlayingOrSpectatingMatch(toRefresh)) {
            return getNameColorMatch(toRefresh, refreshFor);
        } else {
            return getNameColorLobby(toRefresh, refreshFor);
        }
    }

    private static ChatColor getNameColorMatch(Player toRefresh, Player refreshFor) {
        MatchHandler matchHandler = PotPvPRP.getInstance().getMatchHandler();

        Match toRefreshMatch = matchHandler.getMatchPlayingOrSpectating(toRefresh);
        MatchTeam toRefreshTeam = toRefreshMatch.getTeam(toRefresh.getUniqueId());

        // they're a spectator, so we see them as gray
        if (toRefreshTeam == null) {
            return ChatColor.GRAY;
        }

        MatchTeam refreshForTeam = toRefreshMatch.getTeam(refreshFor.getUniqueId());

        // if we can't find a current team, check if they have any
        // previously teams we can use for this
        if (refreshForTeam == null) {
            refreshForTeam = toRefreshMatch.getPreviousTeam(refreshFor.getUniqueId());
        }

        // if we were/are both on teams display a friendly/enemy color
        if (refreshForTeam != null) {
            if (toRefreshTeam == refreshForTeam) {
                return ChatColor.GREEN;
            } else {
                if (ArcherClass.getMarkedPlayers().containsKey(toRefresh.getName()) && System.currentTimeMillis() < ArcherClass.getMarkedPlayers().get(toRefresh.getName())) {
                    return ChatColor.YELLOW;
                }
                return ChatColor.RED;
            }
        }

        // if we're a spectator just display standard colors
        List<MatchTeam> teams = toRefreshMatch.getTeams();

        // we have predefined colors for 'normal' matches
        if (teams.size() == 2) {
            // team 1 = LIGHT_PURPLE, team 2 = AQUA
            if (toRefreshTeam == teams.get(0)) {
                return ChatColor.LIGHT_PURPLE;
            } else {
                return ChatColor.AQUA;
            }
        } else {
            // we don't have colors defined for larger matches
            // everyone is just red for spectators
            return ChatColor.RED;
        }
    }

    private static ChatColor getNameColorLobby(Player toRefresh, Player refreshFor) {
        FollowHandler followHandler = PotPvPRP.getInstance().getFollowHandler();

        Optional<UUID> following = followHandler.getFollowing(refreshFor);
        boolean refreshForFollowingTarget = following.isPresent() && following.get().equals(toRefresh.getUniqueId());

        if (refreshForFollowingTarget) {
            return ChatColor.AQUA;
        } else {
            return Holiday.getInstance().getProfileHandler().getByPlayer(toRefresh).getDisplayRank().getColor();
        }
    }

    @Override
    public List<BufferedNametag> getPlate(Player player) {

        List<BufferedNametag> nametags = new ArrayList<>();

        for (Player toRefresh : PotPvPRP.getInstance().getServer().getOnlinePlayers()) {
            nametags.add(fetchNameTag(toRefresh, player));
        }

        return nametags;
    }

    @Override
    public boolean showHealthBelowName(Player player) {
        MatchHandler matchHandler = PotPvPRP.getInstance().getMatchHandler();
        if (matchHandler.isPlayingMatch(player)) {
            Match match = matchHandler.getMatchPlaying(player);
            return match.getKitType().isHealthShown();
        }
        return false;
    }

    public BufferedNametag fetchNameTag(Player toRefresh, Player refreshFor) {
        ChatColor prefixColor = getNameColor(toRefresh, refreshFor);

        boolean invis = false;
        MatchHandler matchHandler = PotPvPRP.getInstance().getMatchHandler();
        if (matchHandler.isPlayingMatch(toRefresh) && matchHandler.isPlayingMatch(refreshFor)) {
            Match match = matchHandler.getMatchPlaying(toRefresh);
            MatchTeam team = match.getTeam(toRefresh.getUniqueId());
            if (team != null) {
                if (team.getAllMembers().contains(refreshFor.getUniqueId())) {
                    invis = true;
                }
            }
        }

        return new BufferedNametag(toRefresh.getUniqueId().toString().substring(0, 15), prefixColor.toString(), "", invis, toRefresh);
    }
}