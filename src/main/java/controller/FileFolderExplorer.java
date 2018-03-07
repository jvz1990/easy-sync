package controller;

import com.jfoenix.controls.*;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import model.*;
import networking.TCP.Message;
import networking.TCP.SocketProcessor;
import util.General;
import util.Icons;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.function.Function;

public class FileFolderExplorer implements Initializable {

    private final ObservableList<ExplorerRowItem> itemList;
    private final TreeItem<ExplorerRowItem> tableRoot;

    public JFXTextField searchField;
    public Label rowCount;
    public JFXTreeTableView<ExplorerRowItem> treeTableView;
    public JFXTreeTableColumn<ExplorerRowItem, String> fileFolderColumn;
    public JFXTreeTableColumn<ExplorerRowItem, String> fileSizeColumn;
    public JFXTreeTableColumn<ExplorerRowItem, String> dateModifiedColumn;
    public AnchorPane root;
    public ContextMenu contextMenu;
    public MenuItem copyMenuItem;

    private Folder rootFolder;

    private Device device;

    public FileFolderExplorer(Folder folder, Device device) {
        this.rootFolder = folder;
        this.itemList = FXCollections.observableArrayList();
        this.tableRoot = new RecursiveTreeItem<>(itemList, RecursiveTreeObject::getChildren);
        this.device = device;
        this.addFromFolder(folder);

        this.contextMenu = new ContextMenu();
        this.copyMenuItem = new MenuItem("Copy Item");
        this.contextMenu.getItems().add(this.copyMenuItem);

        this.copyMenuItem.setOnAction(event -> {
            Message message = new Message(Message.Commands.DOWNLOAD_REQUEST);
            FileFolder fileFolder = treeTableView.getSelectionModel().getSelectedItem().getValue().getCurrentFile();
            message.aLong = fileFolder.getID();
            if(message.aLong == -1) {
                message.string = new String[1];
                message.string[0] = fileFolder.getAbsolutePath();
            }
            message.device = device;
            SocketProcessor.HOLDER.addMessage(message);
        });

    }

    public void createSyncButtons(JFXTextField field) {
        JFXButton add = new JFXButton("Add Folder");
        add.getStyleClass().add("custom-jfx-button-raised-blue-small");

        add.setOnMouseClicked(event -> {
            if (treeTableView.getSelectionModel().getSelectedItem().getValue().getCurrentFile().isFile()) {
                Main.Holder.snackbar.fireEvent(
                        new JFXSnackbar.SnackbarEvent(
                                "Please select folder!",
                                "CLOSE",
                                3000,
                                false,
                                event1 -> Main.Holder.snackbar.close()
                        )
                );
                return;
            }

            try {
                Socket socket = new Socket(device.getInetAddress(), device.getPortNo());
                Message message = new Message(Message.Commands.GET_ABSOLUTE_PATH);
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                objectOutputStream.writeObject(message);
                objectOutputStream.flush();

                ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
                message = (Message) objectInputStream.readObject();

                objectInputStream.close();
                objectOutputStream.close();
                socket.close();

                final String text = message.string[0];
                Platform.runLater(() -> field.setText(text));

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        });

        Platform.runLater(() -> root.getChildren().add(add));
    }

    private void addFromFolder(Folder folder) {
        System.out.println("Called from addFromFolder");
        this.itemList.clear();
        if (folder.getParent() != null) {
            this.itemList.add(new ExplorerRowItem(folder.getParent(), true));
        }

        if(folder.getID() == -1) {
            Folder downloaded = getFolderContent(folder); // TODO working point?
            if (downloaded != null) {
                downloaded.setParent(folder);
                folder.getChildren().addAll(downloaded.getChildren());
                folder.getFilesInDir().addAll(downloaded.getFilesInDir());
                folder.setFolderID(-2);
            }
        }

        folder.getChildren().forEach(item -> this.itemList.add(new ExplorerRowItem(item, false)));
        folder.getFilesInDir().forEach(item -> this.itemList.add(new ExplorerRowItem(item, false)));
    }

    private Folder getFolderContent(Folder folder) {
        try {
            Socket socket = new Socket(device.getInetAddress(), device.getPortNo());
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());

            Message message = new Message(Message.Commands.GET_UNSSAVED_FOLDER);
            message.string = new String[1];

            message.string[0] = folder.getAbsolutePath();

            objectOutputStream.writeObject(message);

            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            Folder newFolder = (Folder) objectInputStream.readObject();

            objectOutputStream.flush();
            objectOutputStream.close();
            socket.close();
            return newFolder;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void treeMouseEvent(MouseEvent mouseEvent) {
        MouseButton mouseButton = mouseEvent.getButton();

        switch (mouseButton) {
            case PRIMARY:
                if (mouseEvent.getClickCount() == 2 && !treeTableView.getSelectionModel().isEmpty()) {
                    FileFolder fileFolder = treeTableView.getSelectionModel().getSelectedItem().getValue().getCurrentFile();
                    if (!fileFolder.isFile()) {
                        Platform.runLater(() -> addFromFolder((Folder) fileFolder));
                    }
                }
                break;
            case SECONDARY:
                if (contextMenu.isShowing()) contextMenu.hide();
                contextMenu.show(root, mouseEvent.getScreenX(), mouseEvent.getScreenY());
                break;
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        treeTableView.setRoot(tableRoot);
        treeTableView.setShowRoot(false);
        treeTableView.setEditable(false);
        treeTableView.prefWidthProperty().bind(root.widthProperty().subtract(25));
        treeTableView.prefHeightProperty().bind(root.heightProperty().subtract(55));
        fileFolderColumn.prefWidthProperty().bind(treeTableView.widthProperty().multiply(0.6));
        fileSizeColumn.prefWidthProperty().bind(treeTableView.widthProperty().multiply(0.19));
        dateModifiedColumn.prefWidthProperty().bind(treeTableView.widthProperty().multiply(0.19));
        setupCellValueFactory(fileFolderColumn, ExplorerRowItem::fileNameProperty);
        fileFolderColumn.setCellFactory(param -> new FolderCell());
        setupCellValueFactory(fileSizeColumn, ExplorerRowItem::fileSizeProperty);
        setupCellValueFactory(dateModifiedColumn, ExplorerRowItem::dateModifiedProperty);
    }

    public static class ExplorerRowItem extends RecursiveTreeObject<ExplorerRowItem> {

        private FileFolder currentFile;
        private StringProperty fileName = new SimpleStringProperty();
        private StringProperty fileSize = new SimpleStringProperty();
        private StringProperty dateModified = new SimpleStringProperty();
        private boolean isParent = false;

        public ExplorerRowItem(FileFolder currentFile, boolean isParent) {
            this.currentFile = currentFile;
            this.fileName.setValue(currentFile.getName());
            this.fileSize.setValue((currentFile.isFile() ? General.formatSize(((IFile) currentFile).getFileSize()) : ""));
            this.dateModified.setValue(currentFile.isFile() ? General.formatDate(((IFile) currentFile).getDateModified()) : "");
            this.isParent = isParent;
        }

        public StringProperty fileNameProperty() {
            return fileName;
        }

        public StringProperty fileSizeProperty() {
            return fileSize;
        }

        public StringProperty dateModifiedProperty() {
            return dateModified;
        }

        public FileFolder getCurrentFile() {
            return currentFile;
        }
    }

    private static <T> void setupCellValueFactory(JFXTreeTableColumn<ExplorerRowItem, T> column, Function<ExplorerRowItem, ObservableValue<T>> mapper) {
        column.setCellValueFactory((TreeTableColumn.CellDataFeatures<ExplorerRowItem, T> param) -> {
            if (column.validateValue(param)) {
                return mapper.apply(param.getValue().getValue());
            } else {
                return column.getComputedValue(param);
            }
        });
    }

    public class FolderCell extends TreeTableCell<ExplorerRowItem, String> {
        private Label label;
        private ImageView fxIcon;

        FolderCell() {
            this.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            this.label = new Label();
        }

        @Override
        protected void updateItem(String item, boolean empty) {

            super.updateItem(item, empty);
            if (item == null || empty) {
                this.setGraphic(null);
                return;
            }

            String extension;
            if (!this.getTreeTableRow().getTreeItem().getValue().getCurrentFile().isFile()) {
                extension = "fld";
                if (this.getTreeTableRow().getTreeItem().getValue().isParent) {
                    item = "... Go Back";
                }
            } else {
                extension = General.getExtension(item);
            }

            ImageView imageView = DataState.createdIcons.get(extension);
            if (imageView == null) {
                imageView = Icons.addIcon(extension);
            }
            imageView = new ImageView(imageView.getImage());

            this.fxIcon = imageView;
            this.label.setText(" " + item);

            HBox hBox = new HBox();
            if (this.fxIcon == null) {
                hBox.getChildren().add(label);
                System.err.println("Null image: [" + extension + "]");
            } else {
                hBox.getChildren().addAll(fxIcon, label);
            }
            this.setGraphic(hBox);
        }
    }
}

