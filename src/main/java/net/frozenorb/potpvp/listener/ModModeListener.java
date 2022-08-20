package net.frozenorb.potpvp.listener;

import me.andyreckt.holiday.player.staff.event.StaffModeEnterEvent;
import me.andyreckt.holiday.player.staff.event.StaffModeLeaveEvent;
import net.frozenorb.potpvp.PotPvPRP;
import net.frozenorb.potpvp.lobby.LobbyUtils;
import net.frozenorb.potpvp.profile.setting.Setting;
import net.frozenorb.potpvp.profile.setting.event.SettingUpdateEvent;
import net.frozenorb.potpvp.util.InventoryUtils;
import net.frozenorb.potpvp.util.VisibilityUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ModModeListener implements Listener {

    @EventHandler
    public void onModMode(StaffModeEnterEvent event) {
        if (!event.getPlayer().hasMetadata("ModMode")) event.getPlayer().chat("/silent");
    }

    @EventHandler
    public void onModMode(StaffModeLeaveEvent event) {
        if (event.getPlayer().hasMetadata("ModMode")) event.getPlayer().chat("/silent");
    }

    @EventHandler
    public void onSettings(SettingUpdateEvent event) {
        if(event.getSetting() == Setting.PLAYERS_IN_LOBBY) {
            VisibilityUtils.updateVisibility(event.getPlayer());
        }
        if (event.getSetting() == Setting.FLY_IN_LOBBY) {
            if (PotPvPRP.getInstance().getLobbyHandler().isInLobby(event.getPlayer())) LobbyUtils.resetInventory(event.getPlayer());
        }
    }
}
