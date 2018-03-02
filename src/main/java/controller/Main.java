package controller;

import com.jfoenix.controls.JFXScrollPane;
import com.jfoenix.controls.JFXSnackbar;
import com.jfoenix.controls.JFXTabPane;
import javafx.application.Platform;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import model.DataState;
import model.Device;
import model.DeviceTab;

import java.net.URL;
import java.util.ResourceBundle;

public class Main implements Initializable {

    public StackPane root;
    public JFXTabPane tabPane;
    public VBox scrollVbox;
    public Tab settingsTab;
    public ScrollPane scrollPane;

    public JFXSnackbar snackbar;

    public static Main Holder = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (Holder != null) return;
        Holder = this;

        Platform.runLater(() -> DataState.deviceTabs.forEach(deviceTab -> {
            if (deviceTab.isTabOpened()) tabPane.getTabs().add(deviceTab);
        }));

        JFXScrollPane.smoothScrolling(scrollPane);
        snackbar = new JFXSnackbar(root);


        if (DataState.sharedFolder.getValue().equals("Unnamed") || DataState.downloadFolder.getValue().equals("Unnamed")) {
            tabPane.getSelectionModel().select(settingsTab);
            scrollVbox.getChildren().add(0, new Label("Please remember to complete settings!"));
            snackbar.fireEvent(new JFXSnackbar.SnackbarEvent("Please fill in settings!", "CLOSE", 3000, true, event -> snackbar.close()));
        }

    }

    public void addTab(Device device) {
        for (DeviceTab deviceTab : DataState.deviceTabs) {
            if (deviceTab.getDevice().equals(device)) {
                if (deviceTab.isTabOpened()) {
                    tabPane.getSelectionModel().select(deviceTab);
                    return;
                }
                tabPane.getTabs().add(deviceTab);
                deviceTab.setTabOpened(true);
                return;
            }
        }
        final DeviceTab deviceTab = new DeviceTab(device);
        DataState.deviceTabs.add(deviceTab);
        tabPane.getTabs().add(deviceTab);
        tabPane.getSelectionModel().select(deviceTab);
    }

    public void removeTab(MouseEvent mouseEvent) {
        if (mouseEvent.getButton() != MouseButton.PRIMARY) {
            if (tabPane.getSelectionModel().getSelectedIndex() < 2) return;
            DeviceTab deviceTab = (DeviceTab) tabPane.getSelectionModel().getSelectedItem();
            tabPane.getTabs().remove(deviceTab);
            deviceTab.setTabOpened(false);
        }
    }

    public VBox getScrollVbox() {
        return scrollVbox;
    }
}
