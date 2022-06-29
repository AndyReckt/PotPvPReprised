package net.frozenorb.potpvp.listener;

import me.andyreckt.holiday.player.staff.event.StaffModeEnterEvent;
import me.andyreckt.holiday.player.staff.event.StaffModeLeaveEvent;
import net.frozenorb.potpvp.profile.setting.Setting;
import net.frozenorb.potpvp.profile.setting.event.SettingUpdateEvent;
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
    public void onSettingsVisChange(SettingUpdateEvent event) {
        if(event.getSetting().getName().equalsIgnoreCase(Setting.PLAYERS_IN_LOBBY.getName())) {
            VisibilityUtils.updateVisibility(event.getPlayer());
        }
    }



}
