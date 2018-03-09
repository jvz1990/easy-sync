package model;

import com.jfoenix.controls.*;
import com.jfoenix.svg.SVGGlyph;
import controller.FileFolderExplorer;
import controller.Main;
import controller.SyncFolder;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import networking.TCP.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;

public class DeviceTab extends Tab implements Serializable {

    private static final long serialVersionUID = -1304640273772572274L;
    private transient Device device;
    private transient boolean tabOpened;
    private transient ArrayList<model.SyncFolder> syncFolders = new ArrayList<>(0);

    private transient GridPane gridPane;

    public DeviceTab(final Device device) {
        this.device = device;
        this.tabOpened = true;

        createTab(device);
    }

    private void createTab(final Device device) {
        this.setText(device.getMachineName().getValue());
        this.setContent(createPane());
        this.closableProperty().setValue(true);
    }

    private Node createPane() {
        gridPane = new GridPane();
        gridPane.setVgap(5);
        gridPane.setHgap(5);
        gridPane.setPadding(new Insets(25.0));

        createTrustSlider(gridPane);
        createBrowseClient(gridPane);
        createSyncBtn(gridPane);

        createSyncFolders();

        return gridPane;
    }

    private void createSyncFolders() {
        int rowIndex = 3;
        for (model.SyncFolder syncFolder : this.syncFolders) {
            GridPane gp = new GridPane();
            gp.setHgap(5.0);
            Label label = new Label("Sync Folder: "  + syncFolder.getSyncName());
            JFXButton removeSync = new JFXButton("Remove Sync");
            removeSync.getStyleClass().add("custom-jfx-button-raised-red-medium");

            gp.add(label, 0, 0);
            gp.add(removeSync, 1, 0);

            removeSync.setOnMouseClicked(event -> {
                syncFolders.remove(syncFolder);
                gridPane.getChildren().remove(gp);
            });

            gridPane.add(gp, 0, rowIndex++);
        }
    }

    private void createSyncBtn(GridPane gridPane) {
        JFXButton addSyncSetupBtn = new JFXButton("Add Sync Setup");
        addSyncSetupBtn.getStyleClass().add("custom-jfx-button-raised-green");
        addSyncSetupBtn.setOnMouseClicked(event -> new Thread(this::syncSetupEvent).start());

        gridPane.add(addSyncSetupBtn, 0, 2);
        gridPane.add(new Label("Setup a synchronized folder between devices"), 1, 2);
    }

    private void syncSetupEvent() {
        //Check if trusted

        if (!device.isOnline()) {
            Main.Holder.snackbar.fireEvent(
                    new JFXSnackbar.SnackbarEvent(
                            "Device is offline!",
                            "CLOSE",
                            3000, false,
                            event -> Main.Holder.snackbar.close()));
            return;
        }

        Message message = new Message(Message.Commands.PERMISSION);

        try {
            Socket socket = new Socket(device.getInetAddress(), device.getPortNo());
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.writeObject(message);
            objectOutputStream.flush();

            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            message = (Message) objectInputStream.readObject();
            if (message.command == Message.Commands.ERROR) {
                Main.Holder.snackbar.fireEvent(
                        new JFXSnackbar.SnackbarEvent(device.getMachineNameString() + " does not trust you",
                                "CLOSE",
                                3000,
                                true,
                                b -> Main.Holder.snackbar.close()));
                Main.Holder.scrollVbox.getChildren().add(0, new Label("Please make sure [" + device.getMachineNameString()
                        + "] has trusted you"));
            }

            objectInputStream.close();
            objectOutputStream.close();
            socket.close();

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        if (message.command == Message.Commands.ERROR) return;

        showCreateSyncDialog();
    }

    public void createSyncFolder(model.SyncFolder syncFolder) {

        if (syncFolders.contains(syncFolder)) return;
        syncFolders.add(syncFolder);

        GridPane gp = new GridPane();
        gp.setHgap(5.0);

        Label label = new Label("Sync Folder: "  + syncFolder.getSyncName());
        JFXButton removeSync = new JFXButton("Remove Sync");
        removeSync.getStyleClass().add("custom-jfx-button-raised-red-medium");

        gp.add(label, 0, 0);
        gp.add(removeSync, 1, 0);

        removeSync.setOnMouseClicked(event -> {
            syncFolders.remove(syncFolder);
            gridPane.getChildren().remove(gp);
        });

        Platform.runLater(() -> gridPane.add(gp, 0, gridPane.getRowCount() + 1));

    }

    private void showCreateSyncDialog() {

        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("/fxml/Dialog.fxml"));
        fxmlLoader.setController(new SyncFolder(this));
        try {
            JFXDialog jfxDialog = fxmlLoader.load();
            Platform.runLater(() -> {
                Main.Holder.root.getChildren().add(jfxDialog);
                jfxDialog.show(Main.Holder.root);
                jfxDialog.requestFocus();
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void createBrowseClient(GridPane gridPane) {
        JFXButton browseClient = new JFXButton("Explore");
        browseClient.getStyleClass().add("custom-jfx-button-raised-green");
        browseClient.setOnMouseClicked(event -> createDeviceAction());
        gridPane.add(browseClient, 0, 1);
        gridPane.add(new Label("Browse device shared folder"), 1, 1);
    }

    private void createDeviceAction() {
        if (!device.isOnline()) {
            Main.Holder.snackbar.fireEvent(
                    new JFXSnackbar.SnackbarEvent(
                            "Device is offline!",
                            "CLOSE",
                            3000, false,
                            event -> Main.Holder.snackbar.close()));
            return;
        }
        Message message = new Message(Message.Commands.GET_SHARED_FOLDER);

        try {
            Socket socket = new Socket(device.getInetAddress(), device.getPortNo());
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.writeObject(message);
            objectOutputStream.flush();

            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            Object object = objectInputStream.readObject();
            if (object instanceof Folder) {
                Folder folder = (Folder) object;

                Stage stage = new Stage();
                FXMLLoader fxmlLoader = new FXMLLoader();
                fxmlLoader.setLocation(getClass().getResource("/fxml/DeviceFileBrowser.fxml"));
                fxmlLoader.setController(new FileFolderExplorer(folder, device));
                AnchorPane root = fxmlLoader.load();
                JFXDecorator jfxDecorator = new JFXDecorator(stage, root);
                jfxDecorator.setCustomMaximize(true);
                jfxDecorator.setTitle(device.getMachineName().get() + " Files & Folders");
                jfxDecorator.setGraphic(new SVGGlyph(""));

                Scene scene = new Scene(jfxDecorator, DataState.screenWidth * 0.5, DataState.screenHeight * 0.5);

                final ObservableList<String> sheets = scene.getStylesheets();
                sheets.addAll(
                        getClass().getResource("/css/jfoenix-fonts.css").toExternalForm(),
                        getClass().getResource("/css/jfoenix-design.css").toExternalForm(),
                        getClass().getResource("/css/main.css").toExternalForm()
                );

                stage.setScene(scene);
                stage.show();

                DataState.stages.add(stage);

            } else {
                System.err.println("err"); //TODO: spice up
            }
            objectInputStream.close();
            objectOutputStream.close();
            socket.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void createTrustSlider(final GridPane gridPane) {
        JFXToggleButton trustDevice = new JFXToggleButton();
        trustDevice.setText("Allow Full Access");
        Tooltip.install(trustDevice, new Tooltip("This give full access for " + device.getMachineName().get() + " to this device."));
        gridPane.add(trustDevice, 0, 0);
        trustDevice.setSelected(device.isFullAccess());
        Label label = new Label("Warning, this will give end user the ability to remove files!");
        gridPane.add(label, 1, 0);
        Bindings.bindBidirectional(trustDevice.selectedProperty(), device.fullAccessProperty());
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public boolean isTabOpened() {
        return tabOpened;
    }

    public void setTabOpened(boolean tabOpened) {
        this.tabOpened = tabOpened;
    }

    private void writeObject(ObjectOutputStream objectOutputStream) throws IOException {
        objectOutputStream.defaultWriteObject();
        objectOutputStream.writeObject(device);
        objectOutputStream.writeBoolean(tabOpened);
        objectOutputStream.writeObject(syncFolders);
    }

    public void setSyncFolders(ArrayList<model.SyncFolder> syncFolders) {
        this.syncFolders = syncFolders;
    }

    private void readObject(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
        objectInputStream.defaultReadObject();
        setDevice((Device) objectInputStream.readObject());
        setTabOpened(objectInputStream.readBoolean());
        setSyncFolders((ArrayList<model.SyncFolder>) objectInputStream.readObject());
        createTab(device);
    }

}
