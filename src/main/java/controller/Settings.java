package controller;

import com.jfoenix.controls.JFXTextField;
import com.jfoenix.validation.base.ValidatorBase;
import javafx.beans.binding.Bindings;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import model.DataState;
import networking.UDP.Broadcaster;
import util.BackgroundTask;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class Settings implements Initializable {

    public JFXTextField machineName;
    public JFXTextField sharedFolder;
    public JFXTextField downloadFolderField;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupMachineName();
        Bindings.bindBidirectional(sharedFolder.textProperty(), DataState.sharedFolder);
        Bindings.bindBidirectional(downloadFolderField.textProperty(), DataState.downloadFolder);
    }

    private void setupMachineName() {
        machineName.getValidators().add(new ValidatorBase() {
            @Override
            protected void eval() {
                TextInputControl textField = (TextInputControl) srcControl.get();
                if(textField.getLength() > 3 && textField.getLength() < 15) {
                    hasErrors.set(false);
                } else {
                    hasErrors.set(true);
                }
            }
        });

        Bindings.bindBidirectional(machineName.textProperty(), DataState.machineName);

        machineName.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                if (!machineName.validate()) return;
                DataState.setMachineName(machineName.getText());
                Broadcaster.Holder.INSTANCE.Restart();
            }
        });
    }

    public void browseForFolder() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select share directory");
        File file = directoryChooser.showDialog(DataState.Holder.getPrimaryStage());
        if(file == null) return;
        sharedFolder.setText(file.getAbsolutePath());
        BackgroundTask.mineSharedFolder();
    }

    public void browseForDLFolder() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select download directory");
        File file = directoryChooser.showDialog(DataState.Holder.getPrimaryStage());
        if(file == null) return;
        downloadFolderField.setText(file.getAbsolutePath());
    }

    public void setFakeSave(MouseEvent mouseEvent) {
        Main.Holder.scrollVbox.getChildren().add(0, new Label("Settings Saved"));
    }
}
