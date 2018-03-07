package networking.TCP;

import model.Device;

import java.io.Serializable;
import java.net.Socket;

public class Message implements Serializable {
    public enum Commands {
        UPLOAD, DOWNLOAD, DOWNLOAD_REQUEST, PERMISSION,
        SEMI, FULL, READY, GET_SHARED_FOLDER, ERROR, DONE,
        BROWSE_ROOT, GET_ABSOLUTE_PATH, GET_UNSSAVED_FOLDER
    }

    public Message() {
    }

    public Message(Commands command) {
        this.command = command;
    }

    public Commands command = null;
    public String[] string;
    public long aLong;

    public transient Device device = null;
    public transient Socket socket = null;

}
