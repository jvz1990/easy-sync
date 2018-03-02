import javafx.application.Application;
import javafx.stage.Stage;
import model.DataState;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public class Main extends Application {

    public static void main(String[] args) {
        loadState();
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

    }
}
