package net.frozenorb.potpvp.command.impl;

import lombok.SneakyThrows;
import net.frozenorb.potpvp.PotPvPRP;
import net.frozenorb.potpvp.command.PotPvPCommand;
import net.frozenorb.potpvp.hologram.HologramMeta;
import net.frozenorb.potpvp.hologram.HologramType;
import net.frozenorb.potpvp.hologram.PracticeHologram;
import net.frozenorb.potpvp.hologram.impl.GlobalHologram;
import net.frozenorb.potpvp.hologram.impl.KitHologram;
import net.frozenorb.potpvp.kit.kittype.KitType;
import org.bukkit.entity.Player;
import xyz.refinedev.command.annotation.Command;
import xyz.refinedev.command.annotation.OptArg;
import xyz.refinedev.command.annotation.Require;
import xyz.refinedev.command.annotation.Sender;
import xyz.refinedev.command.util.CC;

import java.util.UUID;

/**
 * This Project is property of Refine Development © 2021 - 2022
 * Redistribution of this Project is not allowed
 *
 * @author Drizzy
 * Created: 4/26/2022
 * Project: potpvp-reprised
 */

public class HologramCommands implements PotPvPCommand {

    @Command(name = "create", usage = "<name> <type> [kit]", desc = "Create a hologram")
    @Require("potpvp.hologram") @SneakyThrows
    public void create(@Sender Player sender, String name, HologramType type, @OptArg KitType kitType) {
        PracticeHologram practiceHologram;
        if (type == HologramType.GLOBAL) {
            practiceHologram = new GlobalHologram(PotPvPRP.getInstance());
        } else {
            if (kitType == null) {
                sender.sendMessage(CC.translate("&cPlease provide a valid kitType!"));
                return;
            }
            practiceHologram = new KitHologram(PotPvPRP.getInstance(), kitType);
        }

        HologramMeta meta = new HologramMeta(UUID.randomUUID());
        meta.setLocation(sender.getLocation());
        meta.setName(CC.translate(name));
        meta.setType(practiceHologram instanceof GlobalHologram ? HologramType.GLOBAL : HologramType.KIT);

        practiceHologram.setMeta(meta);

        if (!PotPvPRP.getInstance().getHologramHandler().getHolograms().contains(practiceHologram)) {
            PotPvPRP.getInstance().getHologramHandler().getHolograms().add(practiceHologram);
            PotPvPRP.getInstance().getHologramHandler().save();
            return;
        }
        practiceHologram.spawn();
    }

    @Command(name = "delete", usage = "<name>", desc = "Delete a hologram")
    @Require("potpvp.hologram")
    public void delete(@Sender Player sender, String name) {
        PracticeHologram practiceHologram = PotPvPRP.getInstance().getHologramHandler().getByName(name);
        if (practiceHologram == null) {
            sender.sendMessage(CC.translate("&cHologram not found!"));
            return;
        }
        PotPvPRP.getInstance().getHologramHandler().delete(practiceHologram);
    }

    @Command(name = "move", usage = "<name>", desc = "Moves a hologram")
    @Require("potpvp.hologram")
    public void move(@Sender Player sender, String name) {
        PracticeHologram practiceHologram = PotPvPRP.getInstance().getHologramHandler().getByName(name);
        if (practiceHologram == null) {
            sender.sendMessage(CC.translate("&cHologram not found!"));
            return;
        }
        PotPvPRP.getInstance().getHologramHandler().getHolograms().remove(practiceHologram);
        practiceHologram.getMeta().setLocation(sender.getLocation());
        PotPvPRP.getInstance().getHologramHandler().getHolograms().add(practiceHologram);
    }

    @Command(name = "save", desc = "Save the holograms")
    @Require("potpvp.hologram") @SneakyThrows
    public void save(@Sender Player sender) {
        PotPvPRP.getInstance().getHologramHandler().save();
    }

    @Override
    public String getCommandName() {
        return "prachologram";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"practicehologram", "ph"};
    }
}
