package model;

import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.util.UUID;

public class Device extends RecursiveTreeObject<Device> implements Serializable {
    private static final long serialVersionUID = -7985603775706240851L;

    private InetAddress inetAddress;
    private int portNo;
    private UUID uuid;
    private long lastHeardFrom = System.currentTimeMillis();
    private transient StringProperty machineName = new SimpleStringProperty();
    private transient SimpleBooleanProperty online = new SimpleBooleanProperty();
    private transient SimpleBooleanProperty trusted = new SimpleBooleanProperty();
    private transient SimpleBooleanProperty blocked = new SimpleBooleanProperty();
    private transient SimpleBooleanProperty fullAccess = new SimpleBooleanProperty();

    public Device(InetAddress inetAddress, int portNo, String machineName, UUID uuid,
                  boolean trusted, boolean blocked, boolean online) {
        this.inetAddress = inetAddress;
        this.portNo = portNo;
        this.uuid = uuid;
        setMachineName(machineName);
        setTrusted(trusted);
        setBlocked(blocked);
        setOnline(online);
    }

    public long getLastHeardFrom() {
        return lastHeardFrom;
    }

    public void setLastHeardFrom(long lastHeardFrom) {
        this.lastHeardFrom = lastHeardFrom;
    }

    public InetAddress getInetAddress() {
        return inetAddress;
    }

    public void setInetAddress(InetAddress inetAddress) {
        this.inetAddress = inetAddress;
    }

    public int getPortNo() {
        return portNo;
    }

    public void setPortNo(int portNo) {
        this.portNo = portNo;
    }

    public String getMachineNameString() {
        return machineName.getValue();
    }

    public StringProperty getMachineName() {
        return machineName;
    }

    public void setMachineName(StringProperty machineName) {
        this.machineName = machineName;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public boolean equals(Object obj) {
        return ((Device) obj).getUuid().compareTo(this.getUuid()) == 0;
    }

    public boolean isTrusted() {
        return trusted.get();
    }

    public SimpleBooleanProperty trustedProperty() {
        return trusted;
    }

    public boolean isBlocked() {
        return blocked.get();
    }

    public SimpleBooleanProperty blockedProperty() {
        return blocked;
    }

    public boolean isOnline() {
        return online.get();
    }

    public SimpleBooleanProperty onlineProperty() {
        return online;
    }

    public void setMachineName(String machineName) {
        if (this.machineName == null) this.machineName = new SimpleStringProperty(machineName);
        else this.machineName.set(machineName);
    }

    public void setTrusted(boolean trusted) {
        if(this.trusted == null) this.trusted = new SimpleBooleanProperty(trusted);
        else this.trusted.set(trusted);
    }

    public void setBlocked(boolean blocked) {
        if(this.blocked == null) this.blocked = new SimpleBooleanProperty(blocked);
        else this.blocked.set(blocked);
    }

    public void setOnline(boolean online) {
        if(this.online == null) this.online = new SimpleBooleanProperty(online);
        else this.online.set(online);
    }

    public void setFullAccess(boolean allowed) {
        if(this.fullAccess == null) this.fullAccess = new SimpleBooleanProperty(allowed);
        else this.fullAccess.set(allowed);
        fullAccess.set(allowed);
    }

    public boolean isFullAccess() {
        return fullAccess.get();
    }

    public SimpleBooleanProperty fullAccessProperty() {
        return fullAccess;
    }



    private void writeObject(ObjectOutputStream objectOutputStream) throws IOException {
        objectOutputStream.defaultWriteObject();
        objectOutputStream.writeObject(inetAddress);
        objectOutputStream.writeInt(portNo);
        objectOutputStream.writeObject(uuid);
        objectOutputStream.writeUTF(getMachineName().getValueSafe());
        objectOutputStream.writeBoolean(isTrusted());
        objectOutputStream.writeBoolean(isBlocked());
        objectOutputStream.writeBoolean(isOnline());
        objectOutputStream.writeBoolean(isFullAccess());
        objectOutputStream.writeLong(lastHeardFrom);

    }

    private void readObject(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
        objectInputStream.defaultReadObject();
        setInetAddress((InetAddress) objectInputStream.readObject());
        setPortNo(objectInputStream.readInt());
        setUuid((UUID) objectInputStream.readObject());
        setMachineName(objectInputStream.readUTF());
        setTrusted(objectInputStream.readBoolean());
        setBlocked(objectInputStream.readBoolean());
        setOnline(objectInputStream.readBoolean());
        setFullAccess(objectInputStream.readBoolean());
        setLastHeardFrom(objectInputStream.readLong());

    }

}
