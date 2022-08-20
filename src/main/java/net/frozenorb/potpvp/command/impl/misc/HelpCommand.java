package net.frozenorb.potpvp.command.impl.misc;

import com.google.common.collect.ImmutableList;

import net.frozenorb.potpvp.PotPvPLang;
import net.frozenorb.potpvp.PotPvPRP;
import net.frozenorb.potpvp.command.PotPvPCommand;
import net.frozenorb.potpvp.match.MatchHandler;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import xyz.refinedev.command.annotation.Command;
import xyz.refinedev.command.annotation.Sender;

import java.util.List;

/**
 * Generic /help command, changes message sent based on if sender is playing in
 * or spectating a match.
 */
public class HelpCommand implements PotPvPCommand {

    private static final List<String> HELP_MESSAGE_HEADER = ImmutableList.of(
        ChatColor.GRAY + PotPvPLang.LONG_LINE,
        "§d§lPractice Help",
        ChatColor.GRAY + PotPvPLang.LONG_LINE,
        "§7§lRemember: §fMost things are clickable!",
        ""
    );

    private static final List<String> HELP_MESSAGE_LOBBY = ImmutableList.of(
        "§bCommon Commands:",
        "§f/duel <player> §7- Challenge a player to a duel",
        "§f/party invite <player> §7- Invite a player to a party",
        "",
        "§bOther Commands:",
        "§f/party help §7- Information on party commands",
        "§f/report <player> <reason> §7- Report a player for violating the rules",
        "§f/request <message> §7- Request assistance from a staff member"
    );

    private static final List<String> HELP_MESSAGE_MATCH = ImmutableList.of(
        "§bCommon Commands:",
        "§f/spectate <player> §7- Spectate a player in a match",
        "§f/report <player> <reason> §7- Report a player for violating the rules",
        "§f/request <message> §7- Request assistance from a staff member"
    );

    private static final List<String> HELP_MESSAGE_FOOTER = ImmutableList.of(
        "",
        "§bServer Information:",
        "§fDiscord §7- §bdiscord.gg/causalmc",
        "§fStore §7- §bstore.causalmc.cc",
        ChatColor.GRAY + PotPvPLang.LONG_LINE
    );

    @Command(name = "", desc = "Help message override")
    public void help(@Sender Player sender) {
        MatchHandler matchHandler = PotPvPRP.getInstance().getMatchHandler();

        HELP_MESSAGE_HEADER.forEach(sender::sendMessage);

        if (matchHandler.isPlayingOrSpectatingMatch(sender)) {
            HELP_MESSAGE_MATCH.forEach(sender::sendMessage);
        } else {
            HELP_MESSAGE_LOBBY.forEach(sender::sendMessage);
        }

        HELP_MESSAGE_FOOTER.forEach(sender::sendMessage);
    }


    @Override
    public String getCommandName() {
        return "help";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"?", "halp", "helpme"};
    }
}
