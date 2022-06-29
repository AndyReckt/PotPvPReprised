package net.frozenorb.potpvp.command.impl.settings;

import net.frozenorb.potpvp.PotPvPRP;
import net.frozenorb.potpvp.command.PotPvPCommand;
import net.frozenorb.potpvp.profile.setting.Setting;
import net.frozenorb.potpvp.profile.setting.SettingHandler;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import xyz.refinedev.command.annotation.Command;
import xyz.refinedev.command.annotation.Sender;

/**
 * /toggleglobalchat command, allows players to toggle {@link Setting#PLAYERS_IN_LOBBY} setting
 */
public class ViewPlayersInLobby implements PotPvPCommand {

    @Command(name = "", desc = "Toggle global chat for your profile")
    public void viewPlayersInLobby(@Sender Player sender) {
        if (!Setting.PLAYERS_IN_LOBBY.canUpdate(sender)) {
            sender.sendMessage(ChatColor.RED + "No permission.");
            return;
        }

        SettingHandler settingHandler = PotPvPRP.getInstance().getSettingHandler();
        boolean enabled = !settingHandler.getSetting(sender, Setting.PLAYERS_IN_LOBBY);

        settingHandler.updateSetting(sender, Setting.PLAYERS_IN_LOBBY, enabled);

        if (enabled) {
            sender.sendMessage(ChatColor.GREEN + "Toggled player visibility on.");
        } else {
            sender.sendMessage(ChatColor.RED + "Toggled player visibility off.");
        }
    }

    @Override
    public String getCommandName() {
        return "viewPlayersInLobby";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"viewplayers", "vpil"};
    }
}