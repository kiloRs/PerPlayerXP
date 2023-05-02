package com.profilesplus.players;

import com.profilesplus.RPGProfiles;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@Getter
public class ProfileBalance {
    private final Profile profile;
    @Setter
    private double balance;

    public ProfileBalance(Profile profile){
        this.profile = profile;
    }
    public void saveBalance(boolean fromPlayer) {
        if (RPGProfiles.isUsingEconomy()) {
            setBalance(fromPlayer?getCurrentFromEconomy():balance);
            profile.getSection().set("balance", balance);
            profile.getInternalPlayer().saveConfig();
        }
    }


    public double getCurrentFromEconomy(){
        return RPGProfiles.getEconomy().getBalance(profile.getInternalPlayer().getPlayer());
    }
    public double loadBalance() {
        if (RPGProfiles.isUsingEconomy()) {
            if (profile.getSection().isDouble("balance")) {
                return balance = profile.getSection().getDouble("balance");
            } else {
                if (profile.isLoaded()) {
                    saveBalance(true);
                    return balance;
                }
            }
        }
        return balance = 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof ProfileBalance that)) return false;

        return new EqualsBuilder().append(profile, that.profile).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(profile).toHashCode();
    }

    public void reload() {
        this.balance = loadBalance();
    }
}
