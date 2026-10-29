package org.cloudplugins.cloudJail.objects;

import org.bukkit.Location;
import java.util.UUID;

public class Punishment {
    public enum Status { ACTIVE, EXPIRED, RELEASED }

    private int id;
    private final String playerName;
    private final UUID playerUUID;
    private final String punisherName;
    private final UUID punisherUUID;
    private final String jailName;
    private final String reason;
    private final long startTime;
    private final long duration;
    private long remainingMillis;
    private Status status;
    private Location lastLocation;
    private boolean pendingTeleport;

    //Для новых записей remaining = duration, id определяется после создания объекта дополнительным запросом
    public Punishment(String playerName, UUID playerUUID, String punisherName, UUID punisherUUID, String jailName, String reason,
                      long startTime, long duration, long remainingMillis, Status status, Location lastLocation) {
        this.playerName = playerName;
        this.playerUUID = playerUUID;
        this.punisherName = punisherName;
        this.punisherUUID = punisherUUID;
        this.jailName = jailName;
        this.reason = reason;
        this.startTime = startTime;
        this.duration = duration;
        this.remainingMillis = remainingMillis;
        this.status = status;
        this.lastLocation = lastLocation;
    }

    public boolean getPendingTeleport() {
        return pendingTeleport;
    }

    public void setPendingTeleport(boolean status) {
        this.pendingTeleport = status;
    }

    public long getRemainingMillis() {
        return remainingMillis;
    }

    public void add(long millis) {
        remainingMillis += millis;
    }

    public void setId(int id){
        this.id = id;
    }

    public boolean tick(long ticks) {
        remainingMillis = Math.max(0, remainingMillis - ticks);
        return remainingMillis == 0;
    }

    public boolean isExpired() {
        return remainingMillis <= 0;
    }

    public int getId() {
        return id;
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

    public String getPunisherName() {
        return punisherName;
    }

    public UUID getPunisherUUID() {
        return punisherUUID;
    }

    public String getReason() {
        return reason;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getDuration() {
        return duration;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Location getLastLocation() {
        return lastLocation;
    }

}
