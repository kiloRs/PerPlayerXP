package com.profilesplus.menu;

import com.profilesplus.RPGProfiles;
import io.lumine.mythic.lib.MythicLib;
import lombok.Getter;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.profess.PlayerClass;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class ClassSelectionMenu extends ConfirmCancelMenu {
    private final ProfileCreateMenu createMenu;
    private String selectedClassType;
    private static final String PERMISSION_PREFIX = "RPGProfiles.class.";

    public ClassSelectionMenu(Player player, ProfileCreateMenu createMenu) {
        super(player, createMenu.plugin, MythicLib.plugin.parseColors("&aSelect Class Type"), calculateInventorySize());
        this.createMenu = createMenu;
        Map<String, ItemStack> classIcons = new HashMap<>();

        for (PlayerClass aClass : MMOCore.plugin.classManager.getAll()) {
            if (!player.hasPermission(PERMISSION_PREFIX + aClass.getId().toLowerCase())){
                RPGProfiles.log("No Permission for: " + aClass.getId() + " on " + player.getName());
                continue;
            }
            if (RPGProfiles.getIcons(player).hasClassIcon(aClass.getId())) {
                RPGProfiles.log("Class Selection Loading: " + aClass.getId());
                classIcons.put(aClass.getId(), RPGProfiles.getIcons(player).getClassIcon(aClass.getId()));
                continue;
            }
            RPGProfiles.log("SKipping class: " + aClass.getId());

        }


        int[] slots = generateSlots(classIcons.size());
        int index = 0;

        for (Map.Entry<String, ItemStack> classIconEntry : classIcons.entrySet()) {
            if (index >= slots.length) {
                break;
            }

            int slot = slots[index++];
            ItemStack classIcon = classIconEntry.getValue();
            String classType = classIconEntry.getKey();

            setItem(slot, classIcon, event -> {
                selectedClassType = classType;
                for (int i : slots) {
                    ItemStack item = inventory.getItem(i);
                    if (item != null) {
                        item.removeEnchantment(Enchantment.ARROW_DAMAGE);
                    }
                }
                classIcon.addUnsafeEnchantment(Enchantment.ARROW_DAMAGE, 1);
                classIcon.editMeta(itemMeta -> itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS));

                updateConfirmButtonAppearance();
            });
        }
    }

    @Override
    protected boolean canConfirm() {
        return selectedClassType != null && MMOCore.plugin.classManager.has(selectedClassType);
    }

    @Override
    protected String failedConfirmMessage() {
        return "Class Selection Failure!";
    }

    @Override
    protected String successfulConfirmMessage() {
        return "Successfully Selected " + selectedClassType;
    }

    @Override
    protected void onConfirm(InventoryClickEvent event) {
        // Set the class type in the ProfileCreateMenu

        createMenu.setClassType(selectedClassType.toUpperCase());

        //Include a way to update the lore of the items here!
        //todo Fix this so the display of the lore input works!

        // Close the ClassSelectionMenu and return to the ProfileCreateMenu
        close(InventoryCloseEvent.Reason.PLUGIN);
        createMenu.open();
    }

    @Override
    protected void onCancel(InventoryClickEvent event) {
        // Close the ClassSelectionMenu and return to the ProfileCreateMenu
        if (createMenu == null){
            this.close(InventoryCloseEvent.Reason.CANT_USE);
        }
        else {
            this.close(InventoryCloseEvent.Reason.PLUGIN);
            createMenu.open();
        }
    }

    @Override
    public List<String> confirmLore() {

        //todo fix lore
        ArrayList<String> strings = new ArrayList<>();
        if (canConfirm()) {
            strings.add(MythicLib.plugin.parseColors("&aSelected Class: " + selectedClassType.toUpperCase()));
        }
        else {
            strings.add(MythicLib.plugin.parseColors("&cPlease select a class first!"));
        }
        return strings;

    }


    @Override
    public void handleCloseEvent(InventoryCloseEvent event) {
        if (event.getPlayer().getUniqueId().equals(player.getUniqueId())){
            if (event.getInventory().getHolder() instanceof ClassSelectionMenu classSelectionMenu){
                if (classSelectionMenu.createMenu == null) {
                    return;
                }
                createMenu.open();
                createMenu.updateConfirmButtonAppearance();
            }
        }
    }
    private static int calculateInventorySize() {
        int numClasses = MMOCore.plugin.classManager.getAll().size();
        int numRows = numClasses > 9 ? 3 : 2;
        return (numRows + 1) * 9 ;
    }
    private static int[] generateSlots(int numClasses) {
        int[] centerRowSlots = {3, 4, 5, 12, 13, 14, 21, 22, 23};

        int[] slots = new int[numClasses];
        System.arraycopy(centerRowSlots, 0, slots, 0, Math.min(numClasses, centerRowSlots.length));
        if (numClasses > centerRowSlots.length) {
            // distribute remaining slots equally on the top and bottom rows
            int remaining = numClasses - centerRowSlots.length;
            int topRowStart = (9 - remaining) / 2;
            int bottomRowStart = 18 + topRowStart;
            for (int i = centerRowSlots.length; i < numClasses; i++) {
                if (i % 2 == 0) {
                    slots[i] = topRowStart + (i - centerRowSlots.length) / 2;
                } else {
                    slots[i] = bottomRowStart + (i - centerRowSlots.length) / 2;
                }
            }
        }

        return slots;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return this.inventory;
    }
}
