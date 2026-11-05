package org.cloudplugins.cloudJail.objects;

import org.bukkit.Location;
import java.util.UUID;

public class Punishment {
    private int id;
    private final String playerName;
    private final UUID playerUUID;
    private final String punisherName;
    private final UUID punisherUUID;
    private final String jailName;
    private final String reason;
    private final long startTimeStamp;
    private final long duration;
    private long remainingMillis;
    private String unjailer = null;
    private String unjailReason = null;
    private Long endTimeStamp = null;
    private PunishmentStatus status;
    private final Location lastLocation;
    private boolean pendingTeleport;


    //Для новых записей remaining = duration, id определяется после создания объекта дополнительным запросом
    public Punishment(String playerName, UUID playerUUID, String punisherName, UUID punisherUUID, String jailName, String reason,
                      long startTimeStamp, long duration, long remainingMillis, PunishmentStatus status, Location lastLocation) {
        this.playerName = playerName;
        this.playerUUID = playerUUID;
        this.punisherName = punisherName;
        this.punisherUUID = punisherUUID;
        this.jailName = jailName;
        this.reason = reason;
        this.startTimeStamp = startTimeStamp;
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

    public long getStartTimeStamp() {
        return startTimeStamp;
    }

    public long getDuration() {
        return duration;
    }

    public PunishmentStatus getStatus() {
        return status;
    }

    public void setStatus(PunishmentStatus status) {
        this.status = status;
    }

    public Location getLastLocation() {
        return lastLocation;
    }

    public void setUnjailer(String unjailer){
        this.unjailer = unjailer;
    }

    public void setUnjailReason(String reason){
        this.unjailReason = reason;
    }

    public void setEndTime(Long endTimeStamp){
        this.endTimeStamp = endTimeStamp;
    }

    public String getUnjailer(){
        return unjailer;
    }

    public String getUnjailReason(){
        return unjailReason;
    }

    public Long getEndTimeStamp(){
        return endTimeStamp;
    }

}
