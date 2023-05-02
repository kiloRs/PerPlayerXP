package com.profilesplus.players;

import com.profilesplus.RPGProfiles;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Stream;

public class ProfileStorage {

    @Getter
    private final @NotNull UUID uniqueId;
    @Setter
    private Map<Integer,Profile> profileMap = new HashMap<>();
    @Getter
    @Setter
    private Profile activeProfile;

    public ProfileStorage(@NotNull UUID uuid){
        this.uniqueId = uuid;

    }
    public ProfileStorage(PlayerData playerData){
        uniqueId = playerData.getPlayer().getUniqueId();
    }

    public Profile getProfile(int number) {
        return profileMap.get(number);
    }

    public void putProfile(Profile profile) {
        profileMap.put(profile.getIndex(),profile);
    }

    public boolean hasProfile(int number) {
        return profileMap.containsKey(number);
    }

    public boolean hasProfile(Profile profile) {
        return profileMap.containsValue(profile);
    }

    public void clearProfiles() {
        profileMap.clear();
    }

    public int getIndexNumber(Profile profile) {
        return hasProfile(profile)?profile.getIndex():0;
    }

    public boolean hasActiveProfile(){
        if (activeProfile != null && !hasProfile(getActiveProfile())){
            throw new RuntimeException("Active Profile Error");
        }
        return activeProfile != null && hasProfile(activeProfile);
    }

    public boolean isEmpty() {
        return profileMap.isEmpty();

    }

    public List<Profile> getAll() {
        return new ArrayList<>(profileMap.values().stream().toList());

    }

    public void remove(int number) {
        Profile remove = profileMap.remove(number);
        if (remove == null){
            RPGProfiles.debug("Cannot remove profile: " + number + " as it is not located.");
        }
    }
    public void removeSpecific(Profile profile){
        profileMap.remove(profile.getIndex(),profile);
    }

    public Profile byName(String name){
        Stream<Profile> stream = profileMap.values().stream();
        return stream.anyMatch(profile -> profile.getId().equalsIgnoreCase(name)) ? stream.filter(profile -> profile.getId().equalsIgnoreCase(name)).findFirst().orElseThrow() : null;
    }

    public Profile get(int index) {
        return profileMap.get(index);
    }

    public boolean isActiveProfile(int indexNumber) {
        return activeProfile.getIndex()==indexNumber;
    }
}
