package org.cloudplugins.cloudJail.objects;

import org.bukkit.Location;
import java.util.UUID;

public class Punishment {
    public enum Status { ACTIVE, EXPIRED, RELEASED }

    private final int id;
    private final UUID playerUUID;
    private final String playerName;
    private final String jailName;
    private final String punisher;
    private final String reason;
    private final long startTime; //БД
    private final long duration; //БД
    private long remainingTime;
    private long expectedEnd;
    private Status status;
    private Location lastLocation;
    private boolean pendingTeleport;

    //Конструктор для новых записей (Id = -1)
    public Punishment(UUID playerUUID, String playerName, String jailName, String punisher, String reason,
                      long startTime, long duration, Status status, Location lastLocation) {
        this(-1, playerUUID, playerName, jailName, punisher, reason, startTime, duration, duration, status, lastLocation);
    }

    //Конструктор для существующих записей, ID из базы + RemainingTime
    public Punishment(int id, UUID playerUUID, String playerName, String jailName, String punisher, String reason,
                      long startTime, long duration, long remainingTime, Status status, Location lastLocation) {
        this.id = id;
        this.playerUUID = playerUUID;
        this.playerName = playerName;
        this.jailName = jailName;
        this.punisher = punisher;
        this.reason = reason;
        this.startTime = startTime;
        this.duration = duration;
        this.remainingTime = remainingTime;
        this.expectedEnd = System.currentTimeMillis() + remainingTime;
        this.status = status;
        this.lastLocation = lastLocation;
    }

    public boolean getPendingTeleport() {
        return pendingTeleport;
    }

    public void setPendingTeleport(boolean status) {
        this.pendingTeleport = status;
    }

    public long getRemainingTime() {
        return remainingTime;
    }

    public boolean isExpired(Long now) {
        return now >= expectedEnd;
    }

    public void extend(long millis) {
        expectedEnd += millis;
    }

    public void updateExpectedEnd(){
        expectedEnd = System.currentTimeMillis() + remainingTime;
    }

    public void updateRemainingTime() {
        remainingTime = Math.max(0, expectedEnd - System.currentTimeMillis());
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

    public String getPunisher() {
        return punisher;
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

    public void setLastLocation(Location lastLocation) {
        this.lastLocation = lastLocation;
    }
}
