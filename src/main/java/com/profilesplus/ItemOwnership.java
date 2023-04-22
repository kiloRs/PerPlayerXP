package com.profilesplus;

import java.util.UUID;

public class ItemOwnership {
    private final UUID owner;
    private final long expiration;

    public ItemOwnership(UUID owner, long expiration) {
        this.owner = owner;
        this.expiration = expiration;
    }

    public UUID getOwner() {
        return owner;
    }

    public long getExpiration() {
        return expiration;
    }
}