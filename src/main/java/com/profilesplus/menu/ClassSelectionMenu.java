package com.profilesplus.menu;

import com.profilesplus.RPGProfiles;
import io.lumine.mythic.lib.MythicLib;
import lombok.Getter;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.profess.PlayerClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
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
    private String selectedClassType;
    private static final String PERMISSION_PREFIX = "RPGProfiles.class.";
    public ClassSelectionMenu(@NotNull Player player, @NotNull ProfileCreateMenu createMenu){
        this(player,createMenu,null);
    }
    public ClassSelectionMenu(@NotNull Player player,@NotNull ProfileCreateMenu createMenu, HumanEntity externalView) {
        super(player, MythicLib.plugin.parseColors("&aSelect Class Type"), calculateInventorySize(),"CLASS_SELECTION",externalView);
        setPreviousMenu(createMenu);
        Map<String, ItemStack> classIcons = new HashMap<>();

        for (PlayerClass aClass : MMOCore.plugin.classManager.getAll()) {
            if (!player.hasPermission(PERMISSION_PREFIX + aClass.getId().toUpperCase())){
                RPGProfiles.log("No Permission for: " + aClass.getId() + " on " + player.getName());
                continue;
            }
            if (RPGProfiles.getIcons(player).hasClassIcon(aClass.getId())) {
                RPGProfiles.log("Class Selection Loading: " + aClass.getId().toUpperCase());
                classIcons.put(aClass.getId().toUpperCase(), RPGProfiles.getIcons(player).getClassIcon(aClass.getId()));
                continue;
            }
            RPGProfiles.log("SKipping class: " + aClass.getId().toUpperCase());

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
                selectedClassType = classType.toUpperCase();
                for (int i : slots) {
                    ItemStack item = inventory.getItem(i);
                    if (item != null && item.equals(classIcon)) {
                        item.addUnsafeEnchantment(Enchantment.ARROW_DAMAGE,1);
                        item.editMeta(itemMeta -> itemMeta.addItemFlags(ItemFlag.HIDE_DYE,ItemFlag.HIDE_ITEM_SPECIFICS,ItemFlag.HIDE_ENCHANTS));
                        item.editMeta(itemMeta -> itemMeta.displayName(Component.text(MythicLib.plugin.parseColors("&a[ACTIVE] " + ((TextComponent) itemMeta.displayName().asComponent()).content()))));
                    }
                }

                updateConfirmButtonAppearance();
            });
        }
    }

    @Override
    protected boolean canConfirm() {
        return selectedClassType != null && !selectedClassType.isEmpty() && player.hasPermission(PERMISSION_PREFIX + selectedClassType.toUpperCase()) && MMOCore.plugin.classManager.has(selectedClassType);
    }

    @Override
    protected String failedConfirmMessage() {
        return RPGProfiles.getMessage(player,"profile.creation.set.class.failure","&eYou must select a class or have permission to use the selected class!!");
    }

    @Override
    protected String successfulConfirmMessage() {
        return RPGProfiles.getMessage(player,"profile.creation.set.class.successful","&eYou have successfully selected " + selectedClassType.toUpperCase() + "!");
    }

    @Override
    protected void onConfirm(InventoryClickEvent event) {
        // Set the class type in the ProfileCreateMenu

        if (hasPreviousMenu(ProfileCreateMenu.class)){
            ((ProfileCreateMenu) getPreviousMenu()).setClassType(selectedClassType.toUpperCase());
            close(CloseReason.CONFIRM_OPEN_NEW);
            return;
        }

        close(CloseReason.CONFIRM);

    }

    @Override
    protected void onCancel(InventoryClickEvent event) {
        // Close the ClassSelectionMenu and return to the ProfileCreateMenu
        if (hasPreviousMenu(ProfileCreateMenu.class)){
            close(CloseReason.CANCEL_OPEN_NEW);
            return;
        }
        close(CloseReason.CANCEL);
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
        switch (getCloseReason()){
            case NONE -> {
                RPGProfiles.debug("No Close Reason in ClassSelectionMenu!");
            }
            case ERROR -> throw new RuntimeException("ERROR: Class Selection Menu");
            case CONFIRM -> {
                if (hasPreviousMenu(ProfileCreateMenu.class)){
                    ((ProfileCreateMenu) getPreviousMenu()).setClassType(selectedClassType.toUpperCase());
                }
            }
            case CONFIRM_OPEN_NEW -> {
                if (hasPreviousMenu(ProfileCreateMenu.class)){
                    ((ProfileCreateMenu) getPreviousMenu()).setClassType(selectedClassType.toUpperCase());
                    getPreviousMenu().open();
                }
            }
            case CANCEL_OPEN_NEW -> {
                if (hasPreviousMenu()){
                    getPreviousMenu().open();
                }
            }

        }
    }
    private static int calculateInventorySize() {
        int numClasses = MMOCore.plugin.classManager.getAll().size();
        int numRows = numClasses > 9 ? 3 : 2;
        return (numRows + 1) * 9 ;
    }
    private static int[] generateSlots(int numClasses) {
        int[] centerRowSlots = {3, 4, 11, 12, 13, 14, 15, 22, 23};

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof ClassSelectionMenu that)) return false;

        return new EqualsBuilder().appendSuper(super.equals(o)).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).appendSuper(super.hashCode()).toHashCode();
    }

    @Override
    public void clear() {

        selectedClassType = null;
        setPreviousMenu(null);
    }
}
