package net.frozenorb.potpvp.command.impl.duel;

import net.frozenorb.potpvp.PotPvPLang;
import net.frozenorb.potpvp.PotPvPRP;
import net.frozenorb.potpvp.arena.ArenaSchematic;
import net.frozenorb.potpvp.arena.menu.select.SelectArenaMenu;
import net.frozenorb.potpvp.command.PotPvPCommand;
import net.frozenorb.potpvp.match.duel.DuelHandler;
import net.frozenorb.potpvp.match.duel.DuelInvite;
import net.frozenorb.potpvp.match.duel.PartyDuelInvite;
import net.frozenorb.potpvp.match.duel.PlayerDuelInvite;
import net.frozenorb.potpvp.kit.kittype.KitType;
import net.frozenorb.potpvp.kit.kittype.menu.select.SelectKitTypeMenu;
import net.frozenorb.potpvp.lobby.LobbyHandler;
import net.frozenorb.potpvp.party.Party;
import net.frozenorb.potpvp.party.PartyHandler;
import net.frozenorb.potpvp.validation.PotPvPValidation;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import xyz.refinedev.command.annotation.Command;
import xyz.refinedev.command.annotation.Sender;

public final class DuelCommand implements PotPvPCommand {

    @Command(name = "", usage = "<target>", desc = "Duel a player")
    public void duel(@Sender Player sender, Player target) {
        if (sender == target) {
            sender.sendMessage(ChatColor.RED + "You can't duel yourself!");
            return;
        }

        PartyHandler partyHandler = PotPvPRP.getInstance().getPartyHandler();
        LobbyHandler lobbyHandler = PotPvPRP.getInstance().getLobbyHandler();

        Party senderParty = partyHandler.getParty(sender);
        Party targetParty = partyHandler.getParty(target);

        if (senderParty != null && targetParty != null) {
            // party dueling party (legal)
            if (!PotPvPValidation.canSendDuel(senderParty, targetParty, sender)) {
                return;
            }

            new SelectKitTypeMenu(kitType -> {
                sender.closeInventory();

                // reassign these fields so that any party changes
                // (kicks, etc) are reflected now
                Party newSenderParty = partyHandler.getParty(sender);
                Party newTargetParty = partyHandler.getParty(target);

                if (newSenderParty != null && newTargetParty != null) {
                    if (newSenderParty.isLeader(sender.getUniqueId())) {
                        new SelectArenaMenu(kitType, arena -> {
                            sender.closeInventory();
                            duel(sender, newSenderParty, newTargetParty, kitType, arena);
                        }, "Select an arena...").openMenu(sender);
                    } else {
                        sender.sendMessage(PotPvPLang.NOT_LEADER_OF_PARTY);
                    }
                }
            }, "Select a kit type...").openMenu(sender);
        } else if (senderParty == null && targetParty == null) {
            // player dueling player (legal)
            if (!PotPvPValidation.canSendDuel(sender, target)) {
                return;
            }

            if (target.hasPermission("potpvp.famous") && System.currentTimeMillis() - lobbyHandler.getLastLobbyTime(target) < 3_000) {
                sender.sendMessage(ChatColor.RED + target.getName() + " just returned to the lobby, please wait a moment.");
                return;
            }

            new SelectKitTypeMenu(kitType -> {
                sender.closeInventory();
                new SelectArenaMenu(kitType, arena -> {
                    sender.closeInventory();
                    duel(sender, target, kitType, arena);
                }, "Select an arena...").openMenu(sender);
            }, "Select a kit type...").openMenu(sender);
        } else if (senderParty == null) {
            // player dueling party (illegal)
            sender.sendMessage(ChatColor.RED + "You must create a party to duel " + target.getName() + "'s party.");
        } else {
            // party dueling player (illegal)
            sender.sendMessage(ChatColor.RED + "You must leave your party to duel " + target.getName() + ".");
        }
    }

    public void duel(@Sender Player sender, Player target, KitType kitType, ArenaSchematic arena) {
        if (!PotPvPValidation.canSendDuel(sender, target, arena)) {
            return;
        }

        DuelHandler duelHandler = PotPvPRP.getInstance().getDuelHandler();
        DuelInvite autoAcceptInvite = duelHandler.findInvite(target, sender);

        // if two players duel each other for the same thing automatically
        // accept it to make their life a bit easier.
        if (autoAcceptInvite != null && autoAcceptInvite.getKitType() == kitType) {
            new AcceptCommand().accept(sender, target);
            return;
        }

        DuelInvite alreadySentInvite = duelHandler.findInvite(sender, target);

        if (alreadySentInvite != null) {
            if (alreadySentInvite.getKitType() == kitType) {
                sender.sendMessage(ChatColor.WHITE + "You have already invited " + ChatColor.AQUA + target.getName() + ChatColor.WHITE + " to a " + kitType.getColoredDisplayName() + ChatColor.WHITE + " duel.");
                return;
            } else {
                // if an invite was already sent (with a different kit type)
                // just delete it (so /accept will accept the 'latest' invite)
                duelHandler.removeInvite(alreadySentInvite);
            }
        }

        target.sendMessage(ChatColor.AQUA + sender.getName() + ChatColor.WHITE + " has sent you a " + kitType.getColoredDisplayName() + ChatColor.WHITE + " duel on arena " + ChatColor.AQUA + arena.getName() + ChatColor.WHITE + ".");
        target.spigot().sendMessage(createInviteNotification(sender.getName()));

        sender.sendMessage(ChatColor.WHITE + "Successfully sent a " + kitType.getColoredDisplayName() + ChatColor.WHITE + " duel invite to " + ChatColor.AQUA + target.getName() + ChatColor.WHITE + ".");
        duelHandler.insertInvite(new PlayerDuelInvite(sender, target, kitType, arena));
    }

    public void duel(@Sender Player sender, Party senderParty, Party targetParty, KitType kitType, ArenaSchematic arena) {
        if (!PotPvPValidation.canSendDuel(senderParty, targetParty, sender, arena)) {
            return;
        }

        DuelHandler duelHandler = PotPvPRP.getInstance().getDuelHandler();
        DuelInvite autoAcceptInvite = duelHandler.findInvite(targetParty, senderParty);
        String targetPartyLeader = PotPvPRP.getInstance().getUuidCache().name(targetParty.getLeader());

        // if two players duel each other for the same thing automatically
        // accept it to make their life a bit easier.
        if (autoAcceptInvite != null && autoAcceptInvite.getKitType() == kitType) {
            new AcceptCommand().accept(sender, Bukkit.getPlayer(targetParty.getLeader()));
            return;
        }

        DuelInvite alreadySentInvite = duelHandler.findInvite(senderParty, targetParty);

        if (alreadySentInvite != null) {
            if (alreadySentInvite.getKitType() == kitType) {
                sender.sendMessage(ChatColor.WHITE + "You have already invited " + ChatColor.AQUA + targetPartyLeader + "'s party" + ChatColor.WHITE + " to a " + kitType.getColoredDisplayName() + ChatColor.WHITE + " duel.");
                return;
            } else {
                // if an invite was already sent (with a different kit type)
                // just delete it (so /accept will accept the 'latest' invite)
                duelHandler.removeInvite(alreadySentInvite);
            }
        }

        targetParty.message(ChatColor.AQUA + sender.getName() + "'s Party (" + senderParty.getMembers().size() + ")" + ChatColor.WHITE + " has sent you a " + kitType.getColoredDisplayName() + ChatColor.WHITE + " duel on arena " + ChatColor.AQUA + arena.getName() + ChatColor.WHITE + ".");
        Bukkit.getPlayer(targetParty.getLeader()).spigot().sendMessage(createInviteNotification(sender.getName()));

        sender.sendMessage(ChatColor.WHITE + "Successfully sent a " + kitType.getColoredDisplayName() + ChatColor.WHITE + " duel invite to " + ChatColor.AQUA + targetPartyLeader + "'s party" + ChatColor.WHITE + ".");
        duelHandler.insertInvite(new PartyDuelInvite(senderParty, targetParty, kitType, arena));
    }

    private TextComponent[] createInviteNotification(String sender) {
        TextComponent firstPart = new TextComponent("Click here or type " + "/accept " + sender + " to accept the duel.");

        firstPart.setColor(net.md_5.bungee.api.ChatColor.GREEN);
        ClickEvent.Action runCommand = ClickEvent.Action.RUN_COMMAND;
        HoverEvent.Action showText = HoverEvent.Action.SHOW_TEXT;

        firstPart.setClickEvent(new ClickEvent(runCommand, "/accept " + sender));
        firstPart.setHoverEvent(new HoverEvent(showText, new BaseComponent[] { new TextComponent(ChatColor.GREEN + "Click here to accept") }));

        return new TextComponent[] { firstPart};
    }

    @Override
    public String getCommandName() {
        return "duel";
    }

    @Override
    public String[] getAliases() {
        return new String[]{};
    }
}