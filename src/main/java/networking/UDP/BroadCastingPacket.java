package networking.UDP;

import java.io.Serializable;
import java.util.UUID;

public class BroadCastingPacket implements Serializable {
    private static final long serialVersionUID = -760562175086938674L;

    private String machineName;
    private UUID machineUUID;
    private int TCPport;

    public BroadCastingPacket(String machineName, int TCPport, UUID machineUUID) {
        this.machineName = machineName;
        this.TCPport = TCPport;
        this.machineUUID = machineUUID;
    }

    public String getMachineName() {
        return machineName;
    }

    public int getTCPport() {
        return TCPport;
    }

    public UUID getMachineUUID() {
        return machineUUID;
    }
}
