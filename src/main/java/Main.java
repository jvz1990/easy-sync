import com.jfoenix.controls.JFXDecorator;
import com.jfoenix.svg.SVGGlyph;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import model.DataState;
import model.Devices;
import model.Stopable;
import networking.UDP.Broadcaster;
import networking.UDP.Listener;
import util.BackgroundTask;

import java.io.*;
import java.util.ArrayList;

public class Main extends Application {

    private static final ArrayList<Stopable> stopables = new ArrayList<>();

    public static void main(String[] args) {
        loadState();
        launch(args);
    }

    private static void loadState() {
        File file = new File("easy-sync-settings.bin");
        new DataState();
        if (file.exists()) {
            ObjectInputStream objectInputStream = null;
            try {
                objectInputStream = new ObjectInputStream(new FileInputStream(file));
                DataState.Holder = (DataState) objectInputStream.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                if (objectInputStream != null) try {
                    objectInputStream.close();
                } catch (IOException ignore) {
                }
                //noinspection ResultOfMethodCallIgnored
                file.delete();
            }
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(Main.class.getResource("/fxml/Main.fxml"));
        StackPane root = fxmlLoader.load();

        JFXDecorator jfxDecorator = new JFXDecorator(primaryStage, root);
        jfxDecorator.setCustomMaximize(true);
        jfxDecorator.setText("Easy File Sync");
        jfxDecorator.setGraphic(new SVGGlyph(""));

        Scene scene = new Scene(jfxDecorator, DataState.screenWidth * 0.66, DataState.screenHeight * 0.66);

        final ObservableList<String> sheets = scene.getStylesheets();
        sheets.addAll(
                Main.class.getResource("/css/jfoenix-fonts.css").toExternalForm(),
                Main.class.getResource("/css/jfoenix-design.css").toExternalForm(),
                Main.class.getResource("/css/main.css").toExternalForm()

        );

        primaryStage.setScene(scene);
        primaryStage.show();
        DataState.Holder.setPrimaryStage(primaryStage);

        new Thread(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            stopables.add(controller.Devices.getHolder());
            stopables.add(Devices.Holder);
            stopables.add(Broadcaster.Holder.INSTANCE);
            stopables.add(Listener.Holder.INSTANCE);
            stopables.add(networking.TCP.Listener.INSTANCE);

            new BackgroundTask();

            try {
                Thread.sleep(1000);
                System.gc();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        primaryStage.setOnHiding(event -> {
            DataState.stages.forEach(Stage::close);
            Platform.exit();
            try {
                stop();
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        });
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        closeProgram();
    }

    private void closeProgram() {
        Thread closer = new Thread(() -> {
            try {
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream("settings.bin"));
                objectOutputStream.writeObject(DataState.Holder);
                objectOutputStream.flush();
                objectOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }

            for (Stopable stopable : stopables) {
                try {
                    stopable.stopProcess();
                } catch (Exception ignore) {}
            }

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.exit(1);
            }

            System.gc();
            System.exit(0);
        });

        closer.setDaemon(true);
        closer.start();
    }
}
