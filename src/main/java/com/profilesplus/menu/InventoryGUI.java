package com.profilesplus.menu;

import com.profilesplus.RPGProfiles;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Getter
public abstract class InventoryGUI implements InventoryHolder {
    protected final Player player;
    protected final String title;
    protected final Inventory inventory;
    protected final Map<Integer, Consumer<InventoryClickEvent>> actions;
    @Setter
    private InventoryGUI previousMenu;
    private String keyName = null;
    private final String messagePath ;
    private HumanEntity externalView = null;
    private boolean currentlyViewing = false;

    public InventoryGUI(Player player, String title, int size, String key, HumanEntity viewer){
        this.player = player;
        this.title = title;
        this.inventory = Bukkit.getServer().createInventory(this, size, title);
        this.actions = new HashMap<>();
        this.keyName = key;
        this.messagePath = "Menus." + keyName + ".";
        this.externalView = viewer;
        this.currentlyViewing = false;
    }
    public InventoryGUI(Player player, String title, int size, String key) {
        this(player,title,size,key,null);
    }

    public boolean hasExternalViewer(){
        return externalView != null && currentlyViewing;
    }


    public void open() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (externalView != null && !currentlyViewing) {
                    // Open the inventory for the external viewer
                    externalView.openInventory(inventory);
                    currentlyViewing = true;

                    // If the owning player is in the menu, kick them out
                    if (player.getOpenInventory().getTopInventory().equals(inventory)) {
                        player.closeInventory(InventoryCloseEvent.Reason.CANT_USE);
                        player.sendMessage(ChatColor.RED + "You have been kicked out of the menu due to an external viewer.");
                    }
                }
                else if (externalView != null && currentlyViewing){
                    player.sendMessage(ChatColor.RED + "You cannot currently open this menu as an Operator is running a review of your profiles.");
                    return;
                }
                else {
                    player.openInventory(inventory);
                }
            }
        }.runTask(RPGProfiles.getInstance());
    }


    public void setItem(int slot, ItemStack item, Consumer<InventoryClickEvent> onClick) {
        inventory.setItem(slot, item);
        actions.put(slot, onClick);
    }

    public void processClickEvent(InventoryClickEvent event) {

        // Process the click event based on the actions map
        event.setCancelled(true);
        int clickedSlot = event.getRawSlot();
        if (actions.containsKey(clickedSlot)) {
            actions.get(clickedSlot).accept(event);
        }
    }

    public void close(){
        if (hasExternalViewer()){
            getExternalView().closeInventory(InventoryCloseEvent.Reason.PLUGIN);
            return;
        }
        player.closeInventory(InventoryCloseEvent.Reason.PLUGIN);
    }
    public void setExternalVie(HumanEntity human){
        this.externalView = human;
        this.currentlyViewing = false;
    }

    public abstract void handleCloseEvent(InventoryCloseEvent event);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof InventoryGUI that)) return false;


        return new EqualsBuilder().append(getPlayer().getUniqueId(), that.getPlayer().getUniqueId()).append(getKeyName(), that.getKeyName()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(getPlayer()).append(getKeyName()).toHashCode();
    }

    protected boolean hasPreviousMenu(){
        return previousMenu != null;
    }
    protected <A extends InventoryGUI> boolean hasPreviousMenu(Class<A> type){
            if (type.equals(ProfileCreateMenu.class)){
                return hasPreviousMenu() && getPreviousMenu() instanceof ProfileCreateMenu createMenu;
            }
            else if (type.equals(ProfileRemoveMenu.class)){
                return hasPreviousMenu() && getPreviousMenu() instanceof ProfileRemoveMenu profileRemoveMenu;
            }
            else if (type.equals(ClassSelectionMenu.class)){
                return hasPreviousMenu() && getPreviousMenu() instanceof ClassSelectionMenu classSelectionMenu;
            } else if (type.equals(ConfirmCancelMenu.class)) {
                return hasPreviousMenu() && getPreviousMenu() instanceof ConfirmCancelMenu confirmCancelMenu1;
            }
            else if (type.equals(CharSelectionMenu.class)){
                return hasPreviousMenu() && getPreviousMenu() instanceof CharSelectionMenu charSelectionMenu1;
            }
        return false;
    }

    public String getMessage(String useKey, String notFounText){

        return RPGProfiles.getMessage(player, messagePath + useKey, notFounText);
    }
}
