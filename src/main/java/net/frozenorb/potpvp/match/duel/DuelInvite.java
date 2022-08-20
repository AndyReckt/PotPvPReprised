package net.frozenorb.potpvp.match.duel;

import com.google.common.base.Preconditions;

import net.frozenorb.potpvp.arena.ArenaSchematic;
import net.frozenorb.potpvp.kit.kittype.KitType;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import lombok.Getter;

public abstract class DuelInvite<T> {

    @Getter private final T sender;
    @Getter private final T target;
    @Getter private final KitType kitType;
    @Getter private final Instant timeSent;
    @Getter private final ArenaSchematic arena;

    public DuelInvite(T sender, T target, KitType kitType, ArenaSchematic arena) {
        this.sender = Preconditions.checkNotNull(sender, "sender");
        this.target = Preconditions.checkNotNull(target, "target");
        this.kitType = Preconditions.checkNotNull(kitType, "kitType");
        this.timeSent = Instant.now();
        this.arena = Preconditions.checkNotNull(arena, "arena");
    }

    public boolean isExpired() {
        long sentAgo = ChronoUnit.SECONDS.between(timeSent, Instant.now());
        return sentAgo > DuelHandler.DUEL_INVITE_TIMEOUT_SECONDS;
    }

}