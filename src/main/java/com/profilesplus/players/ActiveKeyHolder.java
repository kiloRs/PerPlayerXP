package com.profilesplus.players;

import com.profilesplus.RPGProfiles;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

public class ActiveKeyHolder {
    private final Player player;
    private int active;
    private static final NamespacedKey ACTIVE_PROFILE_KEY = new NamespacedKey(RPGProfiles.getInstance(),"ACTIVE_PROFILE_KEY");


    public ActiveKeyHolder(Player player){
        this.player = player;
        this.active = player.getPersistentDataContainer().getOrDefault(ACTIVE_PROFILE_KEY,PersistentDataType.INTEGER,0);
    }
    public boolean hasActive(){
        return active > 0;
//        return player.getPersistentDataContainer().has(ACTIVE_PROFILE_KEY, PersistentDataType.INTEGER)&&player.getPersistentDataContainer().get(ACTIVE_PROFILE_KEY,PersistentDataType.INTEGER)>0;
    }

    public int getActive() {
        return active;
    }

    public void setActive(Profile profile){
        player.getPersistentDataContainer().set(ACTIVE_PROFILE_KEY,PersistentDataType.INTEGER,profile.getIndex());
        player.saveData();
    }
    public void setActive(int number){
        player.getPersistentDataContainer().set(ACTIVE_PROFILE_KEY,PersistentDataType.INTEGER,number);
        player.saveData();
    }
    public void resetActive(){
        player.getPersistentDataContainer().remove(ACTIVE_PROFILE_KEY);
        player.saveData();
        this.active = 0;
    }
}
