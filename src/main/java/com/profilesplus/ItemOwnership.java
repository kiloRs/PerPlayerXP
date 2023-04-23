package com.profilesplus;

import java.util.UUID;

public record ItemOwnership(UUID owner, long expiration) {
}