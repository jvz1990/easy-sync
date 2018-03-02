package controller;

import com.jfoenix.controls.*;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import model.Device;
import model.Stopable;

import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

public class Devices extends Thread implements Initializable, Stopable {
    private static final String BLUE = "custom-jfx-check-box-all-colored";
    private static final String RED = "custom-jfx-check-box";

    public StackPane centerStackPane;
    public JFXTreeTableView<Device> treeTableView;
    public JFXTreeTableColumn<Device, String> nameColumn;
    public JFXTreeTableColumn<Device, Boolean> onlineColumn;
    public JFXTreeTableColumn<Device, Boolean> trustedColumn;
    public JFXTreeTableColumn<Device, Boolean> blockedColumn;

    private final TreeItem<Device> root = new RecursiveTreeItem<>(State.deviceList, RecursiveTreeObject::getChildren);
    public JFXTextField searchField;
    public Label rowCount;

    private static Devices Holder = null;
    private static final AtomicBoolean keepRunning = new AtomicBoolean(true);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if(Holder != null) return;
        //createBogusDevice();
        createTableColumns();
        createTableView();
        setupSearchField();

        Holder = this;
        start();
    }

    private void createTableColumns() {
        //Create name column
        setupCellValueFactory(nameColumn, Device::getMachineName);
        //Trusted column
        setupCellValueFactory(trustedColumn, Device::trustedProperty);
        trustedColumn.setCellFactory(param -> new BooleanCell(BLUE));
        //Blocked column
        setupCellValueFactory(blockedColumn, Device::blockedProperty);
        blockedColumn.setCellFactory(param -> new BooleanCell(RED));
        //Online column
        setupCellValueFactory(onlineColumn, Device::onlineProperty);
        onlineColumn.setCellFactory(param -> new RadioCell());
    }

    public static Devices getHolder() {
        return Holder;
    }

    @Override
    public void run() {
        while (keepRunning.get()) {

            long now = System.currentTimeMillis();

            State.deviceList.forEach(device -> {
                if (device.getLastHeardFrom() + 5600 < now) {
                    device.setOnline(false);
                } else {
                    device.setOnline(true);
                }
            });

            try {
                Thread.sleep(5509);
            } catch (InterruptedException ignored) {}
        }
    }

    private void createBogusDevice() {
        try {
            State.deviceList.add(new Device(
                    InetAddress.getByName("255.255.255.255"),
                    8888,
                    "bogus",
                    UUID.randomUUID(),
                    false,
                    false,
                    true
            ));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    private void setupSearchField() {
        searchField.textProperty().addListener((observable, oldValue, newValue) ->
                treeTableView.setPredicate(deviceProp ->
                        deviceProp.getValue().getMachineName().toString().toUpperCase().contains(newValue.toUpperCase())));

        rowCount.textProperty().bind(
                Bindings.createStringBinding(
                        () -> "( " + treeTableView.getCurrentItemsCount() + " )",
                        treeTableView.currentItemsCountProperty()
                )
        );
    }

    private void createTableView() {
        treeTableView.setRoot(root);
        treeTableView.setShowRoot(false);
        treeTableView.setEditable(true);
    }

    public static void addToList(Device device) {
        Platform.runLater(() -> {
            if(State.deviceList.contains(device)) return;
            State.deviceList.add(device);
            Holder.searchField.textProperty().set(" ");
            Holder.searchField.textProperty().set("");
        });
    }

    @Override
    public void stopProcess() {
        keepRunning.set(false);
        this.interrupt();
    }

    public void treeListener(KeyEvent keyEvent) {
        if (keyEvent.getCode().equals(KeyCode.DELETE)) {
            Device device = treeTableView.getSelectionModel().getSelectedItem().getValue();
            State.hiddenDevices.add(device.getUuid());
            State.deviceList.remove(device);
            Holder.searchField.textProperty().set(" ");
            Holder.searchField.textProperty().set("");
        }
    }

    public void treeMouseListener(MouseEvent mouseEvent) {
        if(mouseEvent.getClickCount() == 2 && mouseEvent.getButton() == MouseButton.PRIMARY) {
            Main.Holder.addTab(treeTableView.getSelectionModel().getSelectedItem().getValue());
        }
    }

    public class RadioCell extends TreeTableCell<Device, Boolean> {
        private JFXRadioButton radioButton;

        RadioCell() {
            this.radioButton = new JFXRadioButton();
            this.radioButton.setDisable(true);
            this.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        }

        @Override
        public void updateItem(Boolean item, boolean empty) {
            super.updateItem(item, empty);
            if (item == null || empty) {
                this.setGraphic(null);
                return;
            }
            this.setGraphic(this.radioButton);
            if (!isEmpty()) {
                this.radioButton.setSelected(item);
            }
        }

    }

    private static <T> void setupCellValueFactory(JFXTreeTableColumn<Device, T> column, Function<Device, ObservableValue<T>> mapper) {
        column.setCellValueFactory((TreeTableColumn.CellDataFeatures<Device, T> param) -> {
            if (column.validateValue(param)) {
                return mapper.apply(param.getValue().getValue());
            } else {
                return column.getComputedValue(param);
            }
        });
    }

    public class BooleanCell extends TreeTableCell<Device, Boolean> {
        private JFXCheckBox checkBox;

        BooleanCell(String color) {
            this.checkBox = new JFXCheckBox();
            checkBox.setDisable(true);
            checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
                if (isEditing()) {
                    commitEdit(newValue == null ? false : newValue);
                }
            });

            this.checkBox.getStyleClass().add(color);

            this.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            this.setEditable(true);
        }

        @Override
        public void startEdit() {
            if (isEmpty()) {
                return;
            }

            super.startEdit();
            checkBox.setDisable(false);
            checkBox.requestFocus();
        }

        @Override
        public void cancelEdit() {
            super.cancelEdit();
            checkBox.setDisable(true);
        }

        @Override
        public void commitEdit(Boolean value) {
            Device device = super.getTreeTableRow().getItem();
            if (value) {
                if (device.isTrusted() || device.isBlocked()) {
                    checkBox.setDisable(true);
                    checkBox.setSelected(false);
                    super.commitEdit(false);
                    return;
                }
            }
            super.commitEdit(value);
            checkBox.setDisable(true);
        }

        @Override
        public void updateItem(Boolean item, boolean empty) {
            super.updateItem(item, empty);
            if (item == null || empty) {
                this.setGraphic(null);
                return;
            }
            this.setGraphic(checkBox);
            if (!isEmpty()) {
                checkBox.setSelected(item);
            }
        }
    }
}
