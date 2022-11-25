package net.frozenorb.potpvp.match;

import com.google.common.base.Preconditions;
import com.google.common.collect.*;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import me.andyreckt.holiday.utils.CC;
import me.andyreckt.holiday.utils.json.JsonBuilder;
import net.frozenorb.potpvp.PotPvPRP;
import net.frozenorb.potpvp.arena.Arena;
import net.frozenorb.potpvp.kit.Kit;
import net.frozenorb.potpvp.kit.kittype.KitType;
import net.frozenorb.potpvp.lobby.LobbyHandler;
import net.frozenorb.potpvp.match.event.*;
import net.frozenorb.potpvp.match.postmatchinv.PostMatchPlayer;
import net.frozenorb.potpvp.profile.elo.EloCalculator;
import net.frozenorb.potpvp.profile.setting.Setting;
import net.frozenorb.potpvp.profile.setting.SettingHandler;
import net.frozenorb.potpvp.util.*;
import net.frozenorb.potpvp.util.uuid.UUIDCache;
import org.bson.Document;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.stream.Collectors;

@Getter
public final class Match {

    private static final int MATCH_END_DELAY_SECONDS = 3;

    private final String _id = UUID.randomUUID().toString().substring(0, 7);

    private final KitType kitType;
    private final Arena arena;
    private final List<MatchTeam> teams; // immutable so @Getter is ok
    private final Map<UUID, PostMatchPlayer> postMatchPlayers = new HashMap<>();
    private final Set<UUID> spectators = new HashSet<>();

    private MatchTeam winner;
    private MatchEndReason endReason;
    private MatchState state;
    private Date startedAt;
    private Date endedAt;
    private final boolean ranked;

    // we track if matches should give a rematch diamond manually. previously
    // we just checked if both teams had 1 player on them, but this wasn't
    // always accurate. Scenarios like a team split of a 3 man team (with one
    // sitting out) would get treated as a 1v1 when calculating rematches.
    // https://github.com/FrozenOrb/PotPvP-SI/issues/19
    // this will also be set to false for ranked matches (which don't allow
    // rematches)
    private final boolean allowRematches;
    @Setter
    private EloCalculator.Result eloChange;

    // this will keep track of blocks placed by players during this match.
    // it'll only be populated if the KitType allows building in the first place.
    private final Set<BlockVector> placedBlocks = new HashSet<>();

    // we only spectators generate one message (either a join or a leave)
    // per match, to prevent spam. This tracks who has used their one message
    private final transient Set<UUID> spectatorMessagesUsed = new HashSet<>();

    private final Map<UUID, UUID> lastHit = Maps.newHashMap();
    private final Map<UUID, Integer> combos = Maps.newHashMap();
    private final Map<UUID, Integer> totalHits = Maps.newHashMap();
    private final Map<UUID, Integer> longestCombo = Maps.newHashMap();
    private final Map<UUID, Integer> missedPots = Maps.newHashMap();
    private final Set<UUID> allPlayers = Sets.newHashSet();

    private Set<UUID> winningPlayers;
    private Set<UUID> losingPlayers;

    private final Map<MatchTeam, Integer> wins = Maps.newHashMap();
    private final Map<UUID, Kit> usedKit = Maps.newHashMap();

    public Match(KitType kitType, Arena arena, List<MatchTeam> teams, boolean ranked, boolean allowRematches) {
        this.kitType = Preconditions.checkNotNull(kitType, "kitType");
        this.arena = Preconditions.checkNotNull(arena, "arena");
        this.teams = ImmutableList.copyOf(teams);
        this.ranked = ranked;
        this.allowRematches = allowRematches;

        saveState();
    }

    private void saveState() {
        if (kitType.isBuildingAllowed())
            this.arena.takeSnapshot();
    }

    void startCountdown() {
        state = MatchState.COUNTDOWN;

        Map<UUID, Match> playingCache = PotPvPRP.getInstance().getMatchHandler().getPlayingMatchCache();
        Set<Player> updateVisiblity = new HashSet<>();

        for (MatchTeam team : this.getTeams()) {
            for (UUID playerUuid : team.getAllMembers()) {

                if (!team.isAlive(playerUuid))
                    continue;

                Player player = Bukkit.getPlayer(playerUuid);

                playingCache.put(player.getUniqueId(), this);

                Location spawn = (team == teams.get(0) ? arena.getTeam1Spawn() : arena.getTeam2Spawn()).clone();
                Vector oldDirection = spawn.getDirection();
                if (!spawn.getChunk().isLoaded()) spawn.getChunk().load();
                Block block = spawn.getBlock();
                while (block.getRelative(BlockFace.DOWN).getType() == Material.AIR) {
                    block = block.getRelative(BlockFace.DOWN);
                    if (block.getY() <= 0) {
                        block = spawn.getBlock();
                        break;
                    }
                }

                spawn = block.getLocation();
                spawn.setDirection(oldDirection);
                spawn.add(0.5, 0, 0.5);

                player.teleport(spawn);
                player.getInventory().setHeldItemSlot(0);

                updateVisiblity.add(player);
                PatchedPlayerUtils.resetInventory(player, GameMode.SURVIVAL);
            }
        }

        // we wait to update visibility until everyone's been put in the player cache
        // then we update vis, otherwise the update code will see 'partial' views of the
        // match
        updateVisiblity.forEach(VisibilityUtils::updateVisibilityFlicker);
        if (kitType.isStickSpawn()) getAllPlayers().forEach(player ->{
            if (Bukkit.getPlayer(player) != null) {
                PatchedPlayerUtils.lockPos(PotPvPRP.getInstance(), Bukkit.getPlayer(player), kitType.isSumo() ? 3 : 5);
            }
        });

        Bukkit.getPluginManager().callEvent(new MatchCountdownStartEvent(this));

        new BukkitRunnable() {

            int countdownTimeRemaining = kitType.isSumo() ? 3 : 5;

            public void run() {
                if (state != MatchState.COUNTDOWN) {
                    cancel();
                    return;
                }

                if (countdownTimeRemaining == 0) {
                    playSoundAll(Sound.FIREWORK_BLAST, 1F);
                    startMatch();
                    return; // so we don't send '0...' message
                } else if (countdownTimeRemaining <= 3) {
                    playSoundAll(Sound.CLICK, 1F);
                }

                messageAll(ChatColor.GRAY + "The match is starting in " + ChatColor.AQUA + this.countdownTimeRemaining + ChatColor.WHITE + "...");
                countdownTimeRemaining--;
            }

        }.runTaskTimer(PotPvPRP.getInstance(), 0L, 20L);
    }

    private void startMatch() {
        state = MatchState.IN_PROGRESS;
        startedAt = new Date();

        messageAll(ChatColor.WHITE + "Match started.");
        messageAll("");
        messageAll(ChatColor.DARK_RED + ChatColor.BOLD.toString() + "(WARNING) " + ChatColor.RED + "Butterfly clicking / Block glitch is not allowed and they may result into a ban.");
        messageAll("");

        getTeams().forEach(team -> getWins().put(team, 0));
        Bukkit.getPluginManager().callEvent(new MatchStartEvent(this));
    }

    public void endMatch(MatchEndReason reason) {
        // prevent duplicate endings
        if (state == MatchState.ENDING || state == MatchState.TERMINATED) {
            return;
        }

        state = MatchState.ENDING;
        endedAt = new Date();
        endReason = reason;

        try {
            for (MatchTeam matchTeam : this.getTeams()) {
                for (UUID playerUuid : matchTeam.getAllMembers()) {
                    allPlayers.add(playerUuid);
                    if (!matchTeam.isAlive(playerUuid))
                        continue;
                    Player player = Bukkit.getPlayer(playerUuid);

                    postMatchPlayers.computeIfAbsent(playerUuid, v -> new PostMatchPlayer(player, kitType.getHealingMethod(), totalHits.getOrDefault(player.getUniqueId(), 0), longestCombo.getOrDefault(player.getUniqueId(), 0), missedPots.getOrDefault(player.getUniqueId(), 0)));
                }
            }

            messageAll(ChatColor.RED + "Match ended.");
            getWins().clear();
            Bukkit.getPluginManager().callEvent(new MatchEndEvent(this));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        int delayTicks = MATCH_END_DELAY_SECONDS * 20;
        if (JavaPlugin.getProvidingPlugin(this.getClass()).isEnabled()) {
            Bukkit.getScheduler().runTaskLater(PotPvPRP.getInstance(), this::terminateMatch, delayTicks);
        } else {
            this.terminateMatch();
        }
    }

    private void terminateMatch() {
        // prevent double terminations
        if (state == MatchState.TERMINATED) {
            return;
        }

        state = MatchState.TERMINATED;

        // if the match ends before the countdown ends
        // we have to set this to avoid a NPE in Date#from
        if (startedAt == null) {
            startedAt = new Date();
        }

        // if endedAt wasn't set before (if terminateMatch was called directly)
        // we want to make sure we set an ending time. Otherwise we keep the
        // technically more accurate time set in endMatch
        if (endedAt == null) {
            endedAt = new Date();
        }

        this.winningPlayers = winner.getAllMembers();
        this.losingPlayers = teams.stream().filter(team -> team != winner).flatMap(team -> team.getAllMembers().stream()).collect(Collectors.toSet());

        Bukkit.getPluginManager().callEvent(new MatchTerminateEvent(this));

        // we have to make a few edits to the document so we use Gson (which has
        // adapters
        // for things like Locations) and then edit it
        JsonObject document = PotPvPRP.getGson().toJsonTree(this).getAsJsonObject();

        document.addProperty("winner", teams.indexOf(winner)); // replace the full team with their index in the full list
        document.addProperty("arena", arena.getSchematic()); // replace the full arena with its schematic (website doesn't care which copy we
        // used)

        Bukkit.getScheduler().runTaskAsynchronously(PotPvPRP.getInstance(), () -> {
            // The Document#parse call really sucks. It generates literally thousands of
            // objects per call.
            // Hopefully we'll be moving to just posting to a web service soon enough (and
            // then we don't have to run
            // Mongo's stupid JSON parser)
            Document parsedDocument = Document.parse(document.toString());
            parsedDocument.put("startedAt", startedAt);
            parsedDocument.put("endedAt", endedAt);
            MongoUtils.getCollection(MatchHandler.MONGO_COLLECTION_NAME).insertOne(parsedDocument);
        });

        MatchHandler matchHandler = PotPvPRP.getInstance().getMatchHandler();
        LobbyHandler lobbyHandler = PotPvPRP.getInstance().getLobbyHandler();

        Map<UUID, Match> playingCache = matchHandler.getPlayingMatchCache();
        Map<UUID, Match> spectateCache = matchHandler.getSpectatingMatchCache();

        if (kitType.isBuildingAllowed()) {
            arena.restore();
        }
        PotPvPRP.getInstance().getArenaHandler().releaseArena(arena);
        matchHandler.removeMatch(this);

        getTeams().forEach(team -> {
            team.getAllMembers().forEach(player -> {
                if (team.isAlive(player)) {
                    playingCache.remove(player);
                    spectateCache.remove(player);
                    lobbyHandler.returnToLobby(Bukkit.getPlayer(player));
                }
            });
        });

        spectators.forEach(player -> {
            if (Bukkit.getPlayer(player) != null) {
                playingCache.remove(player);
                spectateCache.remove(player);
                lobbyHandler.returnToLobby(Bukkit.getPlayer(player));
            }
        });
    }

    public Set<UUID> getSpectators() {
        return ImmutableSet.copyOf(spectators);
    }

    public Map<UUID, PostMatchPlayer> getPostMatchPlayers() {
        return ImmutableMap.copyOf(postMatchPlayers);
    }

    public void checkEnded() {
        if (state == MatchState.ENDING || state == MatchState.TERMINATED) {
            return;
        }

        if (kitType.getNeededWins() > 1) {
            boolean hasWinner = false;
            for (MatchTeam team : teams) {
                if (wins.getOrDefault(team, 0) >= kitType.getNeededWins()) {
                    this.winner = team;
                    hasWinner = true;
                    endMatch(MatchEndReason.ENEMIES_ELIMINATED);
                }
            }

            if (hasWinner) {
                return;
            }

            boolean restart = false;
            for (MatchTeam team : teams) {
                if (team.getAliveMembers().size() == 0) {
                    restart = true;
                    break;
                }
            }

            if (!restart) {
                return;
            }

            for (MatchTeam team : teams) {
                team.getAliveMembers().clear();
                team.getAllMembers().stream()
                        .map(Bukkit::getOfflinePlayer)
                        .filter(OfflinePlayer::isOnline)
                        .map(OfflinePlayer::getUniqueId)
                        .forEach(uid -> team.getAliveMembers().add(uid));
            }
            Set<Player> updateVisiblity = new HashSet<>();

            for (MatchTeam team : this.getTeams()) {
                for (UUID playerUuid : team.getAllMembers()) {

                    if (!team.isAlive(playerUuid))
                        continue;

                    Player player = Bukkit.getPlayer(playerUuid);

                    Location spawn = (team == teams.get(0) ? arena.getTeam1Spawn() : arena.getTeam2Spawn()).clone();
                    Vector oldDirection = spawn.getDirection();
                    if (!spawn.getChunk().isLoaded()) spawn.getChunk().load();
                    Block block = spawn.getBlock();
                    while (block.getRelative(BlockFace.DOWN).getType() == Material.AIR) {
                        block = block.getRelative(BlockFace.DOWN);
                        if (block.getY() <= 0) {
                            block = spawn.getBlock();
                            break;
                        }
                    }

                    spawn = block.getLocation();
                    spawn.setDirection(oldDirection);
                    spawn.add(0.5, 0, 0.5);

                    player.teleport(spawn);
                    player.getInventory().setHeldItemSlot(0);
                    PatchedPlayerUtils.resetInventory(player, GameMode.SURVIVAL);
                    getUsedKit().getOrDefault(player.getUniqueId(), Kit.ofDefaultKit(kitType)).apply(player);
                    updateVisiblity.add(player);
                }
            }
            updateVisiblity.forEach(VisibilityUtils::updateVisibilityFlicker);

            getAllPlayers().stream()
                    .map(Bukkit::getOfflinePlayer)
                    .filter(OfflinePlayer::isOnline)
                    .map(OfflinePlayer::getUniqueId)
                    .forEach(uid -> {
                        Player player = Bukkit.getPlayer(uid);
                        PatchedPlayerUtils.lockPos(PotPvPRP.getInstance(), player, 3);
                    });


        } else {
            List<MatchTeam> teamsAlive = new ArrayList<>();

            for (MatchTeam team : teams) {
                if (!team.getAliveMembers().isEmpty()) {
                    teamsAlive.add(team);
                }
            }

            if (teamsAlive.size() == 1) {
                this.winner = teamsAlive.get(0);
                endMatch(MatchEndReason.ENEMIES_ELIMINATED);
            }
        }
    }

    public boolean isSpectator(UUID uuid) {
        return spectators.contains(uuid);
    }

    public void addSpectator(Player player, Player target) {
        addSpectator(player, target, false);
    }

    // fromMatch indicates if they were a player immediately before spectating.
    // we use this for things like teleporting and messages
    public void addSpectator(Player player, Player target, boolean fromMatch) {
        if (!fromMatch && state == MatchState.ENDING) {
            player.sendMessage(ChatColor.RED + "This match is no longer available for spectating.");
            return;
        }

        Map<UUID, Match> spectateCache = PotPvPRP.getInstance().getMatchHandler().getSpectatingMatchCache();

        spectateCache.put(player.getUniqueId(), this);
        spectators.add(player.getUniqueId());

        if (!fromMatch) {
            Location tpTo = arena.getSpectatorSpawn();

            if (target != null) {
                // we tp them a bit up so they're not inside of their target
                tpTo = target.getLocation().clone().add(0, 1.5, 0);
            }

            player.teleport(tpTo);
            player.sendMessage(ChatColor.YELLOW + "Now spectating " + ChatColor.AQUA + getSimpleDescription(true) + ChatColor.YELLOW + "...");
            sendSpectatorMessage(player, ChatColor.AQUA + player.getName() + ChatColor.YELLOW + " is now spectating.");
        } else {
            // so players don't accidentally click the item to stop spectating
            player.getInventory().setHeldItemSlot(0);
        }

        VisibilityUtils.updateVisibility(player);
        PatchedPlayerUtils.resetInventory(player, GameMode.CREATIVE, true); // because we're about to reset their inv on a timer
        InventoryUtils.resetInventoryDelayed(player);
        player.setAllowFlight(true);
        player.setFlying(true); // called after PlayerUtils reset, make sure they don't fall out of the sky
        ItemListener.addButtonCooldown(player, 1_500);

        Bukkit.getPluginManager().callEvent(new MatchSpectatorJoinEvent(player, this));
    }

    public void removeSpectator(Player player) {
        removeSpectator(player, true);
    }

    public void removeSpectator(Player player, boolean returnToLobby) {
        Map<UUID, Match> spectateCache = PotPvPRP.getInstance().getMatchHandler().getSpectatingMatchCache();

        spectateCache.remove(player.getUniqueId());
        spectators.remove(player.getUniqueId());
        ItemListener.addButtonCooldown(player, 1_500);

        sendSpectatorMessage(player, ChatColor.AQUA + player.getName() + ChatColor.YELLOW + " is no longer spectating.");

        if (returnToLobby) {
            PotPvPRP.getInstance().getLobbyHandler().returnToLobby(player);
        }

        Bukkit.getPluginManager().callEvent(new MatchSpectatorLeaveEvent(player, this));
    }

    private void sendSpectatorMessage(Player spectator, String message) {
        // see comment on spectatorMessagesUsed field for more
        if (spectator.hasMetadata("ModMode") || !spectatorMessagesUsed.add(spectator.getUniqueId())) {
            return;
        }

        SettingHandler settingHandler = PotPvPRP.getInstance().getSettingHandler();

        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online == spectator) {
                continue;
            }

            boolean sameMatch = isSpectator(online.getUniqueId()) || getTeam(online.getUniqueId()) != null;
            boolean spectatorMessagesEnabled = settingHandler.getSetting(online, Setting.SHOW_SPECTATOR_JOIN_MESSAGES);

            if (sameMatch && spectatorMessagesEnabled) {
                online.sendMessage(message);
            }
        }
    }

    public void markDead(Player player) {
        MatchTeam team = getTeam(player.getUniqueId());

        if (team == null) {
            return;
        }

        if (kitType.isBridges()) {
            markDead(player, null);
            return;
        }

        Map<UUID, Match> playingCache = PotPvPRP.getInstance().getMatchHandler().getPlayingMatchCache();

        team.markDead(player.getUniqueId());
        playingCache.remove(player.getUniqueId());

        postMatchPlayers.put(player.getUniqueId(), new PostMatchPlayer(player, kitType.getHealingMethod(), totalHits.getOrDefault(player.getUniqueId(), 0), longestCombo.getOrDefault(player.getUniqueId(), 0), missedPots.getOrDefault(player.getUniqueId(), 0)));
        checkEnded();
    }

    public void markDead(Player player, Player killer) {
        MatchTeam team = getTeam(player.getUniqueId());

        if (team == null) {
            return;
        }
        UUIDCache uuidCache = PotPvPRP.getInstance().getUuidCache();
        if (getKitType().isBridges()) {
            Location spawnLoc = (teams.get(0) != null && teams.get(0) == team ? getArena().getTeam1Spawn() : getArena().getTeam2Spawn());
            player.teleport(spawnLoc);
            player.setNoDamageTicks(20);
            getUsedKit().getOrDefault(player.getUniqueId(), Kit.ofDefaultKit(getKitType())).apply(player);
            for (MatchTeam team1 : teams) {
                if (getTeam(player.getUniqueId()) == team1) {
                    continue;
                }
                team1.getAllMembers().stream().map(Bukkit::getPlayer).filter(Objects::nonNull)
                        .forEach(p -> p.playSound(p.getLocation(), Sound.ORB_PICKUP, 0.5F, 0.5F));
            }
            if (killer != null) {
                if (getTeam(killer.getUniqueId()) != null) {
                    if (getTeams().contains(getTeam(killer.getUniqueId()))) {
                        messageAll(CC.translate("&b" + uuidCache.name(killer.getUniqueId()) + "&f killed &d" + uuidCache.name(player.getUniqueId()) +"&f."));
                    } else {
                        messageAll(CC.translate("&b" + uuidCache.name(player.getUniqueId()) + " &fdied."));
                    }
                }
            } else {
                messageAll(CC.translate("&b" + uuidCache.name(player.getUniqueId()) + " &fdied."));
            }
        }
    }

    public MatchTeam getTeam(UUID playerUuid) {
        for (MatchTeam team : teams) {
            if (team.isAlive(playerUuid)) {
                return team;
            }
        }

        return null;
    }

    public MatchTeam getPreviousTeam(UUID playerUuid) {
        for (MatchTeam team : teams) {
            if (team.getAllMembers().contains(playerUuid)) {
                return team;
            }
        }

        return null;
    }

    /**
     * Creates a simple, one line description of this match This will include two
     * players (if a 1v1) or player counts and the kit type
     *
     * @return A simple description of this match
     */
    public String getSimpleDescription(boolean includeRankedUnranked) {
        String players;

        if (teams.size() == 2) {
            MatchTeam teamA = teams.get(0);
            MatchTeam teamB = teams.get(1);

            if (teamA.getAliveMembers().size() == 1 && teamB.getAliveMembers().size() == 1) {
                String nameA = PotPvPRP.getInstance().getUuidCache().name(teamA.getFirstAliveMember());
                String nameB = PotPvPRP.getInstance().getUuidCache().name(teamB.getFirstAliveMember());

                players = nameA + " vs " + nameB;
            } else {
                players = teamA.getAliveMembers().size() + " vs " + teamB.getAliveMembers().size();
            }
        } else {
            int numTotalPlayers = 0;

            for (MatchTeam team : teams) {
                numTotalPlayers += team.getAliveMembers().size();
            }

            players = numTotalPlayers + " player fight";
        }

        if (includeRankedUnranked) {
            String rankedStr = ranked ? "Ranked" : "Unranked";
            return players + " (" + rankedStr + " " + kitType.getDisplayName() + ")";
        } else {
            return players;
        }
    }

    /**
     * Sends a basic chat message to all alive participants and spectators
     *
     * @param message the message to send
     */
    public void messageAll(String message) {
        messageAlive(message);
        messageSpectators(message);
    }

    /**
     * Plays a sound for all alive participants and spectators
     *
     * @param sound the Sound to play
     * @param pitch the pitch to play the provided sound at
     */
    public void playSoundAll(Sound sound, float pitch) {
        playSoundAlive(sound, pitch);
        playSoundSpectators(sound, pitch);
    }

    /**
     * Sends a basic chat message to all spectators
     *
     * @param message the message to send
     */
    public void messageSpectators(String message) {
        for (UUID spectator : spectators) {
            Player spectatorBukkit = Bukkit.getPlayer(spectator);

            if (spectatorBukkit != null) {
                spectatorBukkit.sendMessage(message);
            }
        }
    }

    /**
     * Plays a sound for all spectators
     *
     * @param sound the Sound to play
     * @param pitch the pitch to play the provided sound at
     */
    public void playSoundSpectators(Sound sound, float pitch) {
        for (UUID spectator : spectators) {
            Player spectatorBukkit = Bukkit.getPlayer(spectator);

            if (spectatorBukkit != null) {
                spectatorBukkit.playSound(spectatorBukkit.getEyeLocation(), sound, 10F, pitch);
            }
        }
    }

    /**
     * Sends a basic chat message to all alive participants
     *
     * @param message the message to send
     * @see MatchTeam#messageAlive(String)
     */
    public void messageAlive(String message) {
        for (MatchTeam team : teams) {
            team.messageAlive(message);
        }
    }

    /**
     * Plays a sound for all alive participants
     *
     * @param sound the Sound to play
     * @param pitch the pitch to play the provided sound at
     */
    public void playSoundAlive(Sound sound, float pitch) {
        for (MatchTeam team : teams) {
            team.playSoundAlive(sound, pitch);
        }
    }

    /**
     * Records a placed block during this match. Used to keep track of which blocks
     * can be broken.
     */
    public void recordPlacedBlock(Block block) {
        placedBlocks.add(block.getLocation().toVector().toBlockVector());
    }

    /**
     * Checks if a block can be broken in this match. Only used if the KitType
     * allows building.
     */
    public boolean canBeBroken(Block block) {
        return (kitType.isSpleef() && (block.getType() == Material.SNOW_BLOCK || block.getType() == Material.GRASS || block.getType() == Material.DIRT)) || placedBlocks.contains(block.getLocation().toVector().toBlockVector());
    }


    public void loadChunks() {
        getArena().getBounds().getChunks().forEach(chunk -> {
            if (!chunk.isLoaded()) {
                chunk.load();
            }
        });
    }
}