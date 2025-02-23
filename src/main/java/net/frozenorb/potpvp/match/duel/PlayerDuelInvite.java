package net.frozenorb.potpvp.match.duel;

import java.util.UUID;

import net.frozenorb.potpvp.arena.ArenaSchematic;
import org.bukkit.entity.Player;

import net.frozenorb.potpvp.kit.kittype.KitType;

public final class PlayerDuelInvite extends DuelInvite<UUID> {

    public PlayerDuelInvite(Player sender, Player target, KitType kitType, ArenaSchematic arena) {
        super(sender.getUniqueId(), target.getUniqueId(), kitType, arena);
    }

}