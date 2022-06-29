package net.frozenorb.potpvp.util;

import me.andyreckt.holiday.Holiday;
import me.andyreckt.holiday.player.ProfileHandler;
import net.frozenorb.potpvp.PotPvPRP;
import net.frozenorb.potpvp.profile.follow.FollowHandler;
import net.frozenorb.potpvp.match.Match;
import net.frozenorb.potpvp.match.MatchHandler;
import net.frozenorb.potpvp.party.Party;
import net.frozenorb.potpvp.party.PartyHandler;
import net.frozenorb.potpvp.profile.setting.Setting;
import net.frozenorb.potpvp.profile.setting.SettingHandler;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class VisibilityUtils {

    public void updateVisibilityFlicker(Player target) {
        for (Player otherPlayer : Bukkit.getOnlinePlayers()) {
            target.hidePlayer(otherPlayer);
            otherPlayer.hidePlayer(target);
        }

        Bukkit.getScheduler().runTaskLater(PotPvPRP.getInstance(), () -> updateVisibility(target), 10L);
    }

    public void updateVisibility(Player target) {
        for (Player otherPlayer : Bukkit.getOnlinePlayers()) {
            if (shouldSeePlayer(otherPlayer, target)) {
                otherPlayer.showPlayer(target);
            } else {
                otherPlayer.hidePlayer(target);
            }

            if (shouldSeePlayer(target, otherPlayer)) {
                target.showPlayer(otherPlayer);
            } else {
                target.hidePlayer(otherPlayer);
            }
        }
    }

    private boolean shouldSeePlayer(Player viewer, Player target) {
        SettingHandler settingHandler = PotPvPRP.getInstance().getSettingHandler();
        FollowHandler followHandler = PotPvPRP.getInstance().getFollowHandler();
        PartyHandler partyHandler = PotPvPRP.getInstance().getPartyHandler();
        MatchHandler matchHandler = PotPvPRP.getInstance().getMatchHandler();
        ProfileHandler profileHandler = Holiday.getInstance().getProfileHandler();

        Match targetMatch = matchHandler.getMatchPlayingOrSpectating(target);

        boolean targetStaffMode = (profileHandler.getByPlayer(target).isInStaffMode() && !profileHandler.getByPlayer(viewer).isInStaffMode());

        if (targetMatch == null) {
            // we're not in a match so we hide other players based on their party/match/rank
            Party targetParty = partyHandler.getParty(target);
            Optional<UUID> following = followHandler.getFollowing(viewer);

            boolean viewerPlayingMatch = matchHandler.isPlayingOrSpectatingMatch(viewer);
            boolean viewerSameParty = targetParty != null && targetParty.isMember(viewer.getUniqueId());
            boolean viewerFollowingTarget = following.isPresent() && following.get().equals(target.getUniqueId());
            boolean targetIsRanked = settingHandler.getSetting(viewer, Setting.PLAYERS_IN_LOBBY) && target.hasPermission("potpvp.donator") ;

            return !targetStaffMode && (viewerPlayingMatch || viewerSameParty || viewerFollowingTarget || targetIsRanked);
        } else {
            // we're in a match so we only hide other spectators (if our settings say so)
            boolean targetIsSpectator = targetMatch.isSpectator(target.getUniqueId());
            boolean viewerSpecSetting = settingHandler.getSetting(viewer, Setting.VIEW_OTHER_SPECTATORS);
            boolean viewerIsSpectator = matchHandler.isSpectatingMatch(viewer);


            return !targetIsSpectator || (viewerSpecSetting && viewerIsSpectator && !target.hasMetadata("ModMode") && !targetStaffMode);
        }
    }

}