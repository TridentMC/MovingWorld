package com.tridevmc.movingworld.common.entity;

import java.util.UUID;

/**
 * Extend from this class to make ship info for your own needs.
 */
public class MovingWorldInfo {

    private String name;
    private UUID owner;

    public MovingWorldInfo() {
        name = "";
        owner = null;
    }

    public String getName() {
        return name;
    }

    public MovingWorldInfo setName(String name) {
        this.name = name;
        return this;
    }

    public UUID getOwner() {
        return owner;
    }

    public MovingWorldInfo setOwner(UUID owner) {
        this.owner = owner;
        return this;
    }

}