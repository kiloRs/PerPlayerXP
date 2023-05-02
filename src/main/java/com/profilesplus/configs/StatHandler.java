package com.profilesplus.configs;

import com.profilesplus.RPGProfiles;
import lombok.Getter;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.profess.ClassOption;
import net.Indyuce.mmocore.api.player.profess.PlayerClass;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Getter
public class StatHandler {
    private static final Map<PlayerClass, SavedInformation> savedClassInformationMap = new HashMap<>();
    private final RPGProfiles profiles;

    public StatHandler(RPGProfiles profiles){
        this.profiles = profiles;
        if (MMOCore.plugin.classManager.getAll().isEmpty()){
            throw new RuntimeException("No Classes from MMOCore!");
        }
        for (PlayerClass aClass : MMOCore.plugin.classManager.getAll()) {
            if (aClass.hasOption(ClassOption.DEFAULT)){
                continue;
            }
            if (!aClass.hasOption(ClassOption.DISPLAY)){
                RPGProfiles.debug("Skipping Starting Values of " + aClass.getId() + " because it is marked as not to display!");
                continue;
            }
            RPGProfiles.getSavingManager().saveStarting(aClass);

            SavedInformation classInformation = new SavedInformation(MMOCore.plugin.dataProvider.getDataManager().getDefaultData());
            put(aClass.getId(),classInformation);
        }
    }

    public void reload() {
        if (MMOCore.plugin.classManager.getAll().isEmpty()){
            throw new RuntimeException("No Classes from MMOCore!");
        }
        for (PlayerClass aClass : MMOCore.plugin.classManager.getAll()) {
            if (aClass.hasOption(ClassOption.DEFAULT)){
                continue;
            }
            if (!aClass.hasOption(ClassOption.DISPLAY)){
                RPGProfiles.debug("Skipping Starting Values of " + aClass.getId() + " because it is marked as not to display!");
                continue;
            }
            if (!RPGProfiles.getDefaultPlayerConfig().isConfigurationSection(aClass.getId().toUpperCase())){
                RPGProfiles.getSavingManager().saveStarting(aClass);

            SavedInformation classInformation = new SavedInformation(MMOCore.plugin.dataProvider.getDataManager().getDefaultData());
            put(aClass.getId(),classInformation);
        }}
    }
    public static Map<PlayerClass, SavedInformation> getAll() {
        return Collections.unmodifiableMap(savedClassInformationMap);
    }

    public boolean has(String className){
        return savedClassInformationMap.containsKey(MMOCore.plugin.classManager.getOrThrow(className.toUpperCase()));
    }
    public SavedInformation get(String className){
        return savedClassInformationMap.getOrDefault(MMOCore.plugin.classManager.getOrThrow(className.toUpperCase()), null);
    }
    public void put(String nameOfClass, SavedInformation information){
        savedClassInformationMap.put(MMOCore.plugin.classManager.getOrThrow(nameOfClass.toUpperCase()),information);
    }
    public void remove(String className){
        savedClassInformationMap.remove(MMOCore.plugin.classManager.getOrThrow(className.toUpperCase()));
    }
}
