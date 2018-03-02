package controller;

import com.jfoenix.controls.*;
import com.jfoenix.svg.SVGGlyph;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import model.DataState;
import model.DeviceTab;
import model.Folder;
import networking.TCP.Message;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class SyncFolder implements Initializable {

    public JFXDialog root;
    public Label heading;
    public JFXDialogLayout dialogLayout;
    public JFXRadioButton optionOne;
    public JFXRadioButton optionTwo;
    public JFXRadioButton optionThree;
    public JFXRadioButton optionFour;

    public JFXButton cancelButton;
    public JFXButton addButton;
    public JFXButton browseOne;
    public JFXButton browseTwo;

    public JFXTextField optionOneTextField;
    public JFXTextField optionTwoTextField;

    private DeviceTab deviceTab;

    public SyncFolder(DeviceTab deviceTab) {
        this.deviceTab = deviceTab;
    }

    private enum BUTTON {ONE, TWO}

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        heading.setText("Add directory sync with [" + deviceTab.getDevice().getMachineNameString() + "]");

        dialogLayout.prefWidthProperty().bind(Main.Holder.root.widthProperty().multiply(0.70));
        dialogLayout.prefHeightProperty().bind(Main.Holder.root.heightProperty().multiply(0.70));

        optionTwo.setText(deviceTab.getDevice().getMachineNameString());
        optionFour.setText(deviceTab.getDevice().getMachineNameString());

        cancelButton.setOnMouseClicked(event -> {
            root.close();
            new Thread(() -> {

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Platform.runLater(() -> Main.Holder.root.getChildren().remove(root));

            }).start();
        });

        browseOne.setOnMouseClicked(event -> processBrowse(BUTTON.ONE));
        browseTwo.setOnMouseClicked(event -> processBrowse(BUTTON.TWO));

        optionOne.setOnAction(event -> optionFour.setSelected(true));
        optionThree.setOnAction(event -> optionTwo.setSelected(true));

        optionTwo.setOnAction(event -> optionThree.setSelected(true));
        optionFour.setOnAction(event -> optionOne.setSelected(true));
    }

    private void processBrowse(BUTTON button) {
        switch (button) {
            case ONE:
                if (optionOne.isSelected()) {
                    String directoryPath = getDirectoryPath();
                    if (directoryPath == null) return;
                    this.optionOneTextField.setText(directoryPath);
                } else {
                    createFileFolderExplorer(optionOneTextField);
                }
                break;
            case TWO:
                if (optionThree.isSelected()) {
                    String directoryPath = getDirectoryPath();
                    if (directoryPath == null) return;
                    this.optionTwoTextField.setText(directoryPath);
                } else {
                    createFileFolderExplorer(optionTwoTextField);
                }
                break;
        }
    }

    private void createFileFolderExplorer(JFXTextField field) {

        Folder folder = null;
        try {
            Socket socket = new Socket(deviceTab.getDevice().getInetAddress(), deviceTab.getDevice().getPortNo());

            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.writeObject(new Message(Message.Commands.BROWSE_ROOT));
            objectOutputStream.flush();

            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            folder = (Folder) objectInputStream.readObject();
            objectInputStream.close();
            objectOutputStream.close();
            socket.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return;
        }

        Stage stage = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("/resources/fxml/DeviceFileBrowser.fxml"));
        FileFolderExplorer fileFolderExplorer = new FileFolderExplorer(folder, deviceTab.getDevice());
        fileFolderExplorer.createSyncButtons(field);
        fxmlLoader.setController(fileFolderExplorer);
        AnchorPane root = null;
        try {
            root = fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        JFXDecorator jfxDecorator = new JFXDecorator(stage, root);
        jfxDecorator.setCustomMaximize(true);
        jfxDecorator.setText(deviceTab.getDevice().getMachineName().get() + " Select Folder");
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
    }

    private String getDirectoryPath() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select directory for sync location");
        File file = directoryChooser.showDialog(DataState.Holder.getPrimaryStage());
        if (file == null) return null;
        if (!file.isDirectory()) return null;
        return file.getAbsolutePath();
    }
}
