package org.cloudplugins.cloudJail.objects;

import org.bukkit.Location;
import java.util.UUID;

public class Punishment {
    public enum Status { ACTIVE, EXPIRED, RELEASED }

    private final UUID playerUUID;
    private final String playerName;
    private final String jailName;
    private final String punisher; //Консоль???
    private final String reason;
    private final long startTime;
    private final long endTime;
    private Status status;
    private org.bukkit.Location lastLocation;  //попробовать просто с Location, если хуйня - отдельный класс организуем ^_^


    public Punishment(UUID playerUUID, String playerName, String jailName, String punisher, String reason,
                      long startTime, long endTime, Status status, Location lastLocation) {
        this.playerUUID = playerUUID;
        this.playerName = playerName;
        this.jailName = jailName;
        this.punisher = punisher;
        this.reason = reason;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.lastLocation = lastLocation;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() >= endTime;
    }

    public long getRemainingMillis() {
        return Math.max(0, endTime - System.currentTimeMillis());
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getJailName() {
        return jailName;
    }

    public String getPunisher() {
        return punisher;
    }

    public String getReason() {
        return reason; }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setLastLocation(Location lastLocation){
        this.lastLocation = lastLocation;
    }

    public Location getLastLocation(){
        return lastLocation;
    }

}
