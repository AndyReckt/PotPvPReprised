package net.frozenorb.potpvp.match.event;

import lombok.Getter;
import net.frozenorb.potpvp.match.Match;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * @author LBuddyBoy (lbuddyboy.me)
 * 02/08/2021 / 10:00 AM
 * potpvp-si / net.frozenorb.potpvp.match.event
 */
public class BridgeEnterLavaPortalEvent extends Event {

	private static HandlerList handlers = new HandlerList();

	@Getter private final Player player;
	@Getter private final Match match;

	public BridgeEnterLavaPortalEvent(Player player, Match match) {
		this.player = player;
		this.match = match;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
