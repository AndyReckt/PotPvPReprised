package net.frozenorb.potpvp.kit.kittype.menu.manage;

import com.google.common.collect.ImmutableList;

import net.frozenorb.potpvp.arena.menu.manage.ManageMenu;
import net.frozenorb.potpvp.kit.kittype.KitType;
import net.frozenorb.potpvp.util.menu.BooleanTraitButton;
import net.frozenorb.potpvp.util.menu.Button;
import net.frozenorb.potpvp.util.menu.IntegerTraitButton;
import net.frozenorb.potpvp.util.menu.Menu;

import net.frozenorb.potpvp.util.menu.buttons.BackButton;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Mazen Kotb
 */
public class ManageKitTypeMenu extends Menu {

    private final KitType type;

    public ManageKitTypeMenu(KitType type) {
        setNoncancellingInventory(true);
        setUpdateAfterClick(true);

        this.type = type;
    }

    @Override
    public String getTitle(Player player) {
        return "Editing " + type.getDisplayName();
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        // Horizontal row
        for (int i = 1; i <= 8; i++) {
            buttons.put(getSlot(i, 1), Button.placeholder(Material.OBSIDIAN));
        }

        buttons.put(getSlot(0, 1), new IntegerTraitButton<>(type, "Needed Wins", KitType::setNeededWins, KitType::getNeededWins, KitType::saveAsync));

        buttons.put(getSlot(0, 2), new BooleanTraitButton<>(type, "Hidden", KitType::setHidden, KitType::isHidden, KitType::saveAsync));
        buttons.put(getSlot(1, 2), new BooleanTraitButton<>(type, "Editable", KitType::setEditable, KitType::isEditable, KitType::saveAsync));
        buttons.put(getSlot(2, 2), new BooleanTraitButton<>(type, "Editor Item Spawn", KitType::setEditorSpawnAllowed, KitType::isEditorSpawnAllowed, KitType::saveAsync));
        buttons.put(getSlot(3, 2), new BooleanTraitButton<>(type, "Health Shown", KitType::setHealthShown, KitType::isHealthShown, KitType::saveAsync));
        buttons.put(getSlot(4, 2), new BooleanTraitButton<>(type, "Building Allowed", KitType::setBuildingAllowed, KitType::isBuildingAllowed, KitType::saveAsync));
        buttons.put(getSlot(5, 2), new BooleanTraitButton<>(type, "Hardcore Healing", KitType::setHardcoreHealing, KitType::isHardcoreHealing, KitType::saveAsync));
        buttons.put(getSlot(6, 2), new BooleanTraitButton<>(type, "Pearl Damage", KitType::setPearlDamage, KitType::isPearlDamage, KitType::saveAsync));
        buttons.put(getSlot(7, 2), new BooleanTraitButton<>(type, "Supports Ranked", KitType::setSupportsRanked, KitType::isSupportsRanked, KitType::saveAsync));
        buttons.put(getSlot(8, 2), new BooleanTraitButton<>(type, "Boxing", KitType::setBoxfight, KitType::isBoxfight, KitType::saveAsync));
        buttons.put(getSlot(0, 3), new BooleanTraitButton<>(type, "Base Raiding", KitType::setRaiding, KitType::isRaiding, KitType::saveAsync));
        buttons.put(getSlot(1, 3), new BooleanTraitButton<>(type, "Sumo", KitType::setSumo, KitType::isSumo, KitType::saveAsync));
        buttons.put(getSlot(2, 3), new BooleanTraitButton<>(type, "Spleef", KitType::setSpleef, KitType::isSpleef, KitType::saveAsync));
        buttons.put(getSlot(3, 3), new BooleanTraitButton<>(type, "BuildUHC", KitType::setBuildUHC, KitType::isBuildUHC, KitType::saveAsync));
        buttons.put(getSlot(4, 3), new BooleanTraitButton<>(type, "FinalUHC", KitType::setFinalUHC, KitType::isFinalUHC, KitType::saveAsync));
        buttons.put(getSlot(5, 3), new BooleanTraitButton<>(type, "Archer", KitType::setArcher, KitType::isArcher, KitType::saveAsync));
        buttons.put(getSlot(6, 3), new BooleanTraitButton<>(type, "Bridges", KitType::setBridges, KitType::isBridges, KitType::saveAsync));
        buttons.put(getSlot(7, 3), new BooleanTraitButton<>(type, "Stick Spawn", KitType::setStickSpawn, KitType::isStickSpawn, KitType::saveAsync));



        buttons.put(getSlot(8, 5), new Button() {

            @Override
            public String getName(Player player) {
                return ChatColor.RED.toString() + ChatColor.BOLD + "Wipe existing kits";
            }

            @Override
            public List<String> getDescription(Player player) {
                return ImmutableList.of(
                    "",
                    ChatColor.RED + "Removes all saved " + type.getDisplayName() + " kits",
                    ChatColor.RED + "(includes online and offline players)",
                    "",
                    ChatColor.RED + "For safety reasons this button is disabled,",
                    ChatColor.RED + "use /kit wipekits type " + type.getId().toLowerCase()
                );
            }

            @Override
            public Material getMaterial(Player player) {
                return Material.TNT;
            }

        });

        buttons.put(getSlot(0, 0), new SaveKitTypeButton(type));
        buttons.put(getSlot(1, 0), new CancelKitTypeEditButton());

        buttons.put(getSlot(8, 0), new BackButton(new ManageMenu()));

        ItemStack[] kit = type.getEditorItems();
        int x = 0;
        int y = 3;

        for (ItemStack editorItem : kit) {
            if (editorItem != null) {
                if (editorItem.getType() != Material.AIR) {
                    buttons.put(getSlot(x + 2, y + 2), nonCancellingItem(editorItem));
                }
            }

            x++;

            if (x > 6) {
                x = 0;
                y++;

                if (y >= 4 + 3) {
                    break;
                }
            }
        }

        return buttons;
    }

    private Button nonCancellingItem(ItemStack stack) {
        return new Button() {
            @Override
            public ItemStack getButtonItem(Player player) {
                return stack;
            }

            @Override
            public String getName(Player player) {
                return stack.getItemMeta().getDisplayName();
            }

            @Override
            public List<String> getDescription(Player player) {
                return stack.getItemMeta().getLore();
            }

            @Override
            public Material getMaterial(Player player) {
                return stack.getType();
            }

            @Override
            public boolean shouldCancel(Player player, int slot, ClickType clickType) {
                return false;
            }
        };
    }
}
