package model;

import java.io.Serializable;
import java.util.UUID;

public class SyncFolder implements Serializable {
    private boolean hasLocalOrigin;
    private String syncName;
    private String localDirectory;
    private String deviceDirectory;
    private UUID deviceID;

    public SyncFolder(boolean hasLocalOrigin, String syncName, String localDirectory, String deviceDirectory, UUID uuid) {
        this.hasLocalOrigin = hasLocalOrigin;
        this.syncName = syncName;
        this.localDirectory = localDirectory;
        this.deviceDirectory = deviceDirectory;
        this.deviceID = uuid;
    }

    public boolean isHasLocalOrigin() {
        return hasLocalOrigin;
    }

    public String getSyncName() {
        return syncName;
    }

    public String getLocalDirectory() {
        return localDirectory;
    }

    public String getDeviceDirectory() {
        return deviceDirectory;
    }

    public UUID getDeviceID() {
        return deviceID;
    }

    @Override
    public boolean equals(Object obj) {
        SyncFolder syncFolder = (SyncFolder) obj;

        return syncFolder.getDeviceID().equals(this.deviceID) &&
                this.syncName.equals(syncFolder.getSyncName()) &&
                this.localDirectory.equals(syncFolder.getLocalDirectory()) &&
                this.deviceDirectory.equals(syncFolder.getDeviceDirectory());
    }

    @Override
    public String toString() {
        return "Do I have a local Origin?: " +
                hasLocalOrigin +
                "\nSyncName: " +
                syncName +
                "\nLocalDirectory: " +
                localDirectory +
                "\nDeviceDirectory: " +
                deviceDirectory;
    }
}
