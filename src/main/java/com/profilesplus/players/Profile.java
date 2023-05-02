package com.profilesplus.players;

import com.profilesplus.RPGProfiles;
import com.profilesplus.configs.SavedInformation;
import io.lumine.mythic.lib.MythicLib;
import lombok.Getter;
import lombok.Setter;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.player.DefaultPlayerData;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;


@Getter
public class Profile {
    private final String id;
    private final net.Indyuce.mmocore.api.player.PlayerData mmoCorePlayer;
    private final PlayerData internalPlayer;
    private final boolean fromFile;
    private SavedInformation classInformation;
    private final String className;
    private ConfigurationSection section;
    private final long creationTime;
    @Setter
    private boolean created = false;
    @Setter
    private Location lastKnownLocation;
    private final int index;
    private final ProfileBalance profileBalance;
    private final boolean containsValues=false;
    private final int saves ;
    private boolean loaded;
    private int infoNumber;
    private String time;

    public Profile(String id, String className,int slotNumber, UUID player, boolean fromFile){
        this.id = id;
        this.index = slotNumber;
        this.className = className;
        if (!MMOCore.plugin.classManager.has(className.toUpperCase())){
            throw new RuntimeException("Profile Class Not In MMOCORE: " + className);
        }
        this.mmoCorePlayer = MMOCore.plugin.dataProvider.getDataManager().get(player);
        this.internalPlayer = PlayerData.get(player);
        this.fromFile = fromFile;
        this.section = internalPlayer.getConfig().getConfigurationSection("profiles." + this.index);

        this.profileBalance = RPGProfiles.isUsingEconomy()?new ProfileBalance(this):null;

        if (this.section == null ){
            this.section = internalPlayer.getConfig().createSection("profiles." + index);
        }
        infoNumber = 0;
        if (!fromFile || this.section.getKeys(false).isEmpty()) {
            if (RPGProfiles.getStatHandler().has(className.toUpperCase())) {
                infoNumber = 1;
                this.classInformation = RPGProfiles.getStatHandler().get(className.toUpperCase());
            }
            else {
                infoNumber = 2;
                this.classInformation = new SavedInformation(new SavedInformation(new DefaultPlayerData(1,0,0,0,0,0,0,20,0,0,0)));
            }
        }
        else {
            infoNumber = 3;
            this.classInformation = new SavedInformation(this.section);
        }
        this.creationTime = section.getLong("creation-time",0);
        time = null;
        if (this.creationTime>0) {
            Instant instant = Instant.ofEpochMilli(creationTime);
            ZonedDateTime zonedDateTime = instant.atZone(ZoneId.systemDefault());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            time = zonedDateTime.format(formatter);
        }
        this.saves = section.getInt("saves",0);
        this.created = this.section!=null&& !this.section.getKeys(false).isEmpty() && this.classInformation != null &&saves>0 && creationTime >0;
        this.loaded = false;
        RPGProfiles.debug("----------------------------------------------------------------------------------------");
        RPGProfiles.log("Profile Information: " + infoNumber + " for Profile: " + index + " (Created:" + created + "[" + saves + "]" + "[" + time + "]");
        RPGProfiles.log("Profile Balance: " + (profileBalance!=null? String.valueOf(profileBalance.getBalance()) :"Not In Use"));
        RPGProfiles.log("Profile Class Name: " + className.toUpperCase());
        RPGProfiles.log("Profile Name: " + id.toUpperCase());
        RPGProfiles.log("Profile Inventory:" + (hasSavedInventory()? Arrays.stream(RPGProfiles.getInventoryManager().getInventory(internalPlayer.getPlayer())).filter(itemStack -> itemStack!=null&&itemStack.getType()!= Material.AIR).collect(Collectors.toSet()).size() : "Unknown" ) );
        RPGProfiles.debug("----------------------------------------------------------------------------------------");

    }
    private void assignName() {
        String name = getName();
        internalPlayer.getPlayer().displayName(Component.text(name));
        internalPlayer.getPlayer().playerListName(Component.text(name));
        internalPlayer.getPlayer().customName(Component.text(MythicLib.plugin.parseColors(name + "&e[" + internalPlayer.getPlayer().getName() + "]")));

    }

    public void reload(){
        this.update(true);
        if (profileBalance != null)
            profileBalance.reload();


        RPGProfiles.debug("----------------------------------------------------------------------------------------");
        RPGProfiles.log("Profile Information: " + infoNumber + " for Profile: " + index + " (Created:" + created + "[" + saves + "]" + "[" + time + "]");
        RPGProfiles.log("Profile Balance: " + (profileBalance!=null? String.valueOf(profileBalance.getBalance()) :"Not In Use"));
        RPGProfiles.log("Profile Class Name: " + className.toUpperCase());
        RPGProfiles.log("Profile Name: " + id.toUpperCase());
        RPGProfiles.log("Profile Inventory:" + (hasSavedInventory()? Arrays.stream(RPGProfiles.getInventoryManager().getInventory(internalPlayer.getPlayer())).filter(itemStack -> itemStack!=null&&itemStack.getType()!= Material.AIR).collect(Collectors.toSet()).size() : "Unknown" ) );
        RPGProfiles.debug("----------------------------------------------------------------------------------------");    }
    public boolean update(boolean activateMeta) {
        if (!internalPlayer.getProfileStorage().hasActiveProfile() || !internalPlayer.getProfileStorage().isActiveProfile(index)){
            RPGProfiles.debug("Active not found for " + index + " of " + internalPlayer.getPlayer().getName());
            return false;
        }

        // Save the current active profile if it exists
        new BukkitRunnable(){
            @Override
            public void run() {
                try {
                    RPGProfiles.getSavingManager().loadData(Profile.this);
                } catch (Exception e) {
                    loaded = false;
                }
                if (Profile.this.hasSavedInventory()){
                    try {
                        RPGProfiles.getSavingManager().loadInventory(Profile.this);
                    } catch (Exception e) {
                        throw new RuntimeException("Inv Load Error: " + index + " of " + internalPlayer.getPlayer().getName());
                    }
                }
                else {
                    RPGProfiles.debug("Alert - Clearing Inveentory - No Saved Profile Inventory!");
                    internalPlayer.getPlayer().getInventory().clear();
                    internalPlayer.saveActiveProfile(created);
                }
                if (loaded) {
                    assignName();
                }
                else {
                    throw new RuntimeException("Loading Failure for Profile!");
                }
            }
        }.runTask(RPGProfiles.getInstance());

        if (activateMeta){
            if (internalPlayer.getProfileStorage().hasActiveProfile()) {
                internalPlayer.getKeyHolder().setActive(index);
            }
        }
        return loaded;
}


    boolean hasSavedInventory() {
        return RPGProfiles.getInventoryManager().hasSavedInventory(internalPlayer.getPlayer(),index);
    }

    @NotNull(value = "Empty Name")
    public String getName() {
        return StringUtils.capitalize(id);
    }

    public ItemStack getIcon(){
        ItemStack freshIconLore = RPGProfiles.getIcons(internalPlayer.getPlayer()).getSlotIcon(index);
        return freshIconLore.clone();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof Profile profile)) return false;

        return new EqualsBuilder().append(getIndex(), profile.getIndex()).append(getId(), profile.getId()).append(getClassName(), profile.getClassName()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(getId()).append(getClassName()).append(getIndex()).toHashCode();
    }

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }


}
