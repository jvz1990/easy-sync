package model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;
import util.General;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;

/**
 * Class to hold program state.
 * Automatically saved/loaded upon program exit/launch into/from easy-sync-settings.bin
 */
public class DataState implements Serializable {

    private static final long serialVersionUID = -5303996242719723995L;

    private int portNoTCP;
    private UUID uuid;

    public static transient StringProperty machineName = new SimpleStringProperty("Unnamed");
    public static transient StringProperty sharedFolder = new SimpleStringProperty("Unnamed");
    public static transient StringProperty downloadFolder = new SimpleStringProperty("Unnamed");
    public static transient final ObservableList<Device> deviceList = FXCollections.observableArrayList();
    public static transient final ObservableList<DeviceTab> deviceTabs = FXCollections.observableArrayList();
    public static transient final ArrayList<UUID> hiddenDevices = new ArrayList<>();

    public static DataState Holder = null;

    //-------------------------No Saves---------------------------------------
    private transient Stage primaryStage;
    public static transient final ArrayList<Stage> stages = new ArrayList<>();
    public static transient final HashSet<FileFolder> sharedFolderSet = new HashSet<>();
    public static transient final HashSet<ImageViewIcon> createdIcons = new HashSet<>();
    public static transient Folder rootFolder = null;
    public static transient Folder userFolder = null;
    public static transient Folder sysRootFolder = null;
    public static transient double screenWidth, screenHeight;
    //------------------------------------------------------------------------

    public DataState() {
        portNoTCP = 10000 + General.random.nextInt(55534);
        uuid = UUID.randomUUID();
        try {
            machineName.setValue(InetAddress.getLocalHost().getHostName());
        } catch (IOException e) {
            e.printStackTrace();
            machineName.setValue("Unnamed");
        }

        Rectangle2D rectangle2D = Screen.getPrimary().getVisualBounds();
        screenHeight = rectangle2D.getHeight();
        screenWidth = rectangle2D.getWidth();

        Holder = this;
    }

    public int getPortNoTCP() {
        return portNoTCP;
    }

    public void setPortNoTCP(int portNoTCP) {
        this.portNoTCP = portNoTCP;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public static String getMachineName() {
        return machineName.get();
    }

    public static void setMachineName(String machineName) {
        DataState.machineName.set(machineName);
    }

    public static void setSharedFolder(String sharedFolder) {
        DataState.sharedFolder.set(sharedFolder);
    }

    public static String getDownloadFolder() {
        return downloadFolder.getValue();
    }

    public static void setDownloadFolder(String downloadFolder) {
        DataState.downloadFolder.set(downloadFolder);
    }

    private void writeObject(ObjectOutputStream objectOutputStream) throws IOException {
        objectOutputStream.defaultWriteObject();
        objectOutputStream.writeInt(Holder.portNoTCP);
        objectOutputStream.writeObject(Holder.uuid);
        objectOutputStream.writeUTF(machineName.getValueSafe());
        objectOutputStream.writeUTF(sharedFolder.getValueSafe());
        objectOutputStream.writeUTF(downloadFolder.getValueSafe());
        objectOutputStream.writeObject(new ArrayList<Device>(deviceList));
        objectOutputStream.writeObject(new ArrayList<DeviceTab>(deviceTabs));
        objectOutputStream.writeObject(new ArrayList<UUID>(hiddenDevices));
    }

    private void readObject(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
        objectInputStream.defaultReadObject();
        Holder.setPortNoTCP(objectInputStream.readInt());
        Holder.setUuid((UUID) objectInputStream.readObject());
        setMachineName(objectInputStream.readUTF());
        setSharedFolder(objectInputStream.readUTF());
        setDownloadFolder(objectInputStream.readUTF());
        deviceList.addAll((ArrayList<Device>) objectInputStream.readObject());
        deviceTabs.addAll((ArrayList<DeviceTab>) objectInputStream.readObject());
        hiddenDevices.addAll((ArrayList<UUID>) objectInputStream.readObject());
    }

}
