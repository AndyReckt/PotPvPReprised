package net.frozenorb.potpvp.util;

import me.andyreckt.holiday.Holiday;
import org.bukkit.entity.Player;

import java.util.UUID;

public class HolidayUtils {

    public static boolean isInStaffMode(Player player) {
        return Holiday.getInstance().getProfileHandler().getByPlayer(player).isInStaffMode();
    }

    public static boolean isInStaffMode(UUID uuid) {
        return Holiday.getInstance().getProfileHandler().getByUUID(uuid).isInStaffMode();
    }


}
