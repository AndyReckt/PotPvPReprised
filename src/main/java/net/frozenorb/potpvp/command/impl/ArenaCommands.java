package net.frozenorb.potpvp.command.impl;

import lombok.SneakyThrows;
import net.frozenorb.potpvp.PotPvPLang;
import net.frozenorb.potpvp.PotPvPRP;
import net.frozenorb.potpvp.arena.Arena;
import net.frozenorb.potpvp.arena.ArenaGrid;
import net.frozenorb.potpvp.arena.ArenaHandler;
import net.frozenorb.potpvp.arena.ArenaSchematic;
import net.frozenorb.potpvp.command.PotPvPCommand;
import net.frozenorb.potpvp.util.LocationUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import xyz.refinedev.command.annotation.Command;
import xyz.refinedev.command.annotation.Require;
import xyz.refinedev.command.annotation.Sender;
import xyz.refinedev.command.util.CC;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * This Project is property of Refine Development © 2021 - 2022
 * Redistribution of this Project is not allowed
 *
 * @author Drizzy
 * Created: 4/5/2022
 * Project: potpvp-reprised
 */
public class ArenaCommands implements PotPvPCommand {

    private static final String[] HELP_MESSAGE = {
            ChatColor.GRAY + PotPvPLang.LONG_LINE,
            "§d§lArena Commands",
            ChatColor.GRAY + PotPvPLang.LONG_LINE,
            "§c " + PotPvPLang.LEFT_ARROW_NAKED + " §a/arena free",
            "§c " + PotPvPLang.LEFT_ARROW_NAKED + " §a/arena createSchematic <schematic>",
            "§c " + PotPvPLang.LEFT_ARROW_NAKED + " §a/arena listArenas <schematic>",
            "§c " + PotPvPLang.LEFT_ARROW_NAKED + " §a/arena repasteSchematic <schematic>",
            "§c " + PotPvPLang.LEFT_ARROW_NAKED + " §a/arena rescaleall <schematic>",
            "§c " + PotPvPLang.LEFT_ARROW_NAKED + " §a/arena listSchematics",
            ChatColor.GRAY + PotPvPLang.LONG_LINE,

    };

//    @Command(name = "", desc = "Help message for arenas")
//    @Require("potpvp.arena.admin")
//    public void help(@Sender Player sender) {
//        sender.sendMessage(HELP_MESSAGE);
//    }

    @Command(name = "free", desc = "Free all arenas")
    @Require("potpvp.arena.admin")
    public void arenaFree(@Sender Player sender) {
        PotPvPRP.getInstance().getArenaHandler().getGrid().free();
        sender.sendMessage(ChatColor.GREEN + "Arena grid has been freed.");
    }

    @Command(name = "createSchematic", usage = "<schematic>", desc = "Create and load a schematic from world edit as an arena")
    @Require("potpvp.arena.admin")
    public void arenaCreateSchematic(@Sender Player sender, String schematicName) {
        ArenaHandler arenaHandler = PotPvPRP.getInstance().getArenaHandler();

        if (arenaHandler.getSchematic(schematicName) != null) {
            sender.sendMessage(ChatColor.RED + "Schematic " + schematicName + " already exists");
            return;
        }

        ArenaSchematic schematic = new ArenaSchematic(schematicName);
        File schemFile = schematic.getSchematicFile();

        if (!schemFile.exists()) {
            sender.sendMessage(ChatColor.RED + "No file for " + schematicName + " found. (" + schemFile.getPath() + ")");
            return;
        }

        arenaHandler.registerSchematic(schematic);

        try {
            schematic.pasteModelArena();
            arenaHandler.saveSchematics();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        sender.sendMessage(ChatColor.GREEN + "Schematic created.");
    }

    @Command(name = "listArenas", usage = "<schematic>", desc = "List all arenas")
    @Require("potpvp.arena.admin")
    public void arenaListArenas(@Sender Player sender, String schematicName) {
        ArenaHandler arenaHandler = PotPvPRP.getInstance().getArenaHandler();
        ArenaSchematic schematic = arenaHandler.getSchematic(schematicName);

        if (schematic == null) {
            sender.sendMessage(ChatColor.RED + "Schematic " + schematicName + " not found.");
            sender.sendMessage(ChatColor.RED + "List all schematics with /arena listSchematics");
            return;
        }

        sender.sendMessage(ChatColor.RED + "------ " + ChatColor.WHITE + schematic.getName() + " Arenas" + ChatColor.RED + " ------");

        for ( Arena arena : arenaHandler.getArenas(schematic)) {
            String locationStr = LocationUtils.locToStr(arena.getSpectatorSpawn());
            String occupiedStr = arena.isInUse() ? ChatColor.RED + "In Use" : ChatColor.GREEN + "Open";

            sender.sendMessage(arena.getCopy() + ": " + ChatColor.GREEN + locationStr + ChatColor.GRAY + " - " + occupiedStr);
        }
    }

    @Command(name = "repasteSchematic", usage = "<schematic>", desc = "Repaste a schematic's arenas")
    @Require("potpvp.arena.admin")
    public void arenaRepasteSchematic(@Sender Player sender, String schematicName) {
        ArenaHandler arenaHandler = PotPvPRP.getInstance().getArenaHandler();
        ArenaSchematic schematic = arenaHandler.getSchematic(schematicName);

        if (schematic == null) {
            sender.sendMessage(ChatColor.RED + "Schematic " + schematicName + " not found.");
            sender.sendMessage(ChatColor.RED + "List all schematics with /arena listSchematics");
            return;
        }

        int currentCopies = arenaHandler.countArenas(schematic);

        if (currentCopies == 0) {
            sender.sendMessage(ChatColor.RED + "No copies of " + schematic.getName() + " exist.");
            return;
        }

        ArenaGrid arenaGrid = arenaHandler.getGrid();

        sender.sendMessage(ChatColor.GREEN + "Starting...");

        arenaGrid.scaleCopies(schematic, 0, () -> {
            sender.sendMessage(ChatColor.GREEN + "Removed old maps, creating new copies...");

            arenaGrid.scaleCopies(schematic, currentCopies, () -> {
                sender.sendMessage(ChatColor.GREEN + "Repasted " + currentCopies + " arenas using the newest " + schematic.getName() + " schematic.");
            });
        });
    }

    @Command(name = "scale", usage = "<schematic> <count>", desc = "Scale schematics to a specific size")
    @Require("potpvp.arena.admin") @SneakyThrows
    public void arenaScale(@Sender Player sender, String schematicName, int count) {
        ArenaHandler arenaHandler = PotPvPRP.getInstance().getArenaHandler();
        ArenaSchematic schematic = arenaHandler.getSchematic(schematicName);

        if (schematic == null) {
            sender.sendMessage(ChatColor.RED + "Schematic " + schematicName + " not found.");
            sender.sendMessage(ChatColor.RED + "List all schematics with /arena listSchematics");
            return;
        }

        while (arenaHandler.getGrid().isBusy()) {
            sender.sendMessage(ChatColor.RED + "Arena grid is busy, please wait...");
            wait(TimeUnit.SECONDS.toMillis(5));
        }

        sender.sendMessage(ChatColor.GREEN + "Starting rescaling of " + schematic.getName() + " to " + count + " copies...");

        arenaHandler.getGrid().scaleCopies(schematic, count, () -> {
            sender.sendMessage(ChatColor.GREEN + "Scaled " + schematic.getName() + " to " + count + " copies.");
        });
    }

    @Command(name = "rescaleall", desc = "Rescale all schematics and their arenas")
    @Require("potpvp.arena.admin")
    public void arenaRescaleAll(@Sender Player sender) {
        PotPvPRP.getInstance().getArenaHandler().getSchematics().forEach(schematic -> {
            ArenaHandler arenaHandler = PotPvPRP.getInstance().getArenaHandler();
            int totalCopies = arenaHandler.getArenas(schematic).size();

            arenaScale(sender, schematic.getName(), 0);
            arenaScale(sender, schematic.getName(), totalCopies);
        });
    }

    @Command(name = "listSchematics", aliases = "listSchems", desc = "List all potpvp schematics")
    @Require("potpvp.arena.admin")
    public void arenaListSchems(@Sender Player sender) {
        ArenaHandler arenaHandler = PotPvPRP.getInstance().getArenaHandler();
        sender.sendMessage(ChatColor.GRAY + PotPvPLang.LONG_LINE);
        sender.sendMessage(CC.translate("&d&lPotPvP Schematics"));
        sender.sendMessage(ChatColor.GRAY + PotPvPLang.LONG_LINE);
        arenaHandler.getSchematics().forEach(schematic -> {
            int size = arenaHandler.getArenas(schematic).size();
            sender.sendMessage(CC.translate("&c" + schematic.getName() + " &7| &cArenas using: &f" + size));
        });
        sender.sendMessage(ChatColor.GRAY + PotPvPLang.LONG_LINE);
    }

    @Command(name = "setIcon", aliases = "icon", usage = "<schematic>", desc = "Set the icon for an arena")
    @Require("potpvp.arena.admin") @SneakyThrows
    public void icon(@Sender Player sender, String schematicName) {
        ArenaHandler arenaHandler = PotPvPRP.getInstance().getArenaHandler();
        ArenaSchematic schematic = arenaHandler.getSchematic(schematicName);

        if (schematic == null) {
            sender.sendMessage(ChatColor.RED + "Schematic " + schematicName + " not found.");
            sender.sendMessage(ChatColor.RED + "List all schematics with /arena listSchematics");
            return;
        }

        if (sender.getItemInHand() == null) {
            sender.sendMessage(ChatColor.RED + "Please hold an item in your hand.");
            return;
        }

        schematic.setIcon(sender.getItemInHand().getData());
        PotPvPRP.getInstance().getArenaHandler().saveSchematics();
        sender.sendMessage(ChatColor.GREEN + "You've updated this arena's icon.");
    }

    @Override
    public String getCommandName() {
        return "arena";
    }

    @Override
    public String[] getAliases() {
        return new String[]{};
    }
}
