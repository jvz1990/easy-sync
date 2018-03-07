package networking.TCP;

import controller.Main;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import model.*;
import util.BackgroundTask;
import util.General;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Objects;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class SocketProcessor extends Thread implements Stopable {

    private final LinkedTransferQueue<Socket> sockets;
    private static final MessageProcessor messageProcessor = new MessageProcessor();
    private static final AtomicBoolean keepRunning = new AtomicBoolean(true);
    private static final int BLOCK_SIZE = 1024 * 128;

    public static SocketProcessor HOLDER;

    public SocketProcessor(final LinkedTransferQueue<Socket> sockets) {
        this.sockets = sockets;
        messageProcessor.start();
        HOLDER = this;
    }

    @Override
    public void run() {
        while (keepRunning.get()) {
            try {
                Socket socket = sockets.take();
                ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
                Object object = objectInputStream.readObject();
                if (!(object instanceof Message)) continue;
                Message message = (Message) object;
                message.socket = socket;
                messageProcessor.addMessage(message);
            } catch (InterruptedException | IOException | ClassNotFoundException ignore) {
            }
        }

        System.out.println("Quitting SocketProcessor");
    }

    @Override
    public void stopProcess() {
        keepRunning.set(false);
        messageProcessor.stopProcess();
        this.interrupt();
    }

    public void addMessage(Message message) {
        messageProcessor.addMessage(message);
    }

    private static class MessageProcessor extends Thread implements Stopable {

        private static final LinkedTransferQueue<Message> messages = new LinkedTransferQueue<Message>();
        private static final AtomicBoolean keepRunning = new AtomicBoolean(true);

        @Override
        public void run() {
            while (keepRunning.get()) {
                try {
                    Message message = messages.take();
                    if (message.command == null) continue;
                    switch (message.command) {
                        case READY:
                            break;
                        case DOWNLOAD_REQUEST:
                            new Thread(() -> processDownloadRequest(message)).start();
                            break;
                        case PERMISSION:
                            new Thread(() -> processPermissionRequest(message)).start();
                            break;
                        case UPLOAD:
                            new Thread(() -> uploadFileFolder(message)).start();
                            break;
                        case GET_SHARED_FOLDER:
                            uploadFolderInfo(message);
                            break;
                        case BROWSE_ROOT:
                            uploadFolderInfo(message);
                            break;
                        case GET_UNSSAVED_FOLDER:
                            uploadFolderInfo(message);
                            break;
                        case GET_ABSOLUTE_PATH:
                            sendAbsolutePath(message);
                            break;
                    }

                } catch (InterruptedException ignore) {
                }
            }
            System.out.println("Quitting Message Processor");
        }

        private void sendAbsolutePath(Message message) {
            Folder folder = (Folder) DataState.sharedFolderMap.get(message.aLong);
            message.string = new String[1];
            message.string[0] = folder.getAbsolutePath();
            Socket socket = message.socket;
            try {
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                objectOutputStream.writeObject(message);
                objectOutputStream.flush();
                objectOutputStream.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        private void processPermissionRequest(Message message) {
            Socket socket = message.socket;
            ObjectOutputStream objectOutputStream;
            try {
                objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            Message reply = new Message();

            InetAddress address = socket.getInetAddress();
            Device device = DataState.deviceList.parallelStream().filter(item -> item.getInetAddress().equals(address)).findAny().orElse(null);
            if (device == null) reply.command = Message.Commands.ERROR;
            else if (device.isFullAccess()) reply.command = Message.Commands.FULL;
            else if (device.isTrusted()) reply.command = Message.Commands.SEMI;
            else reply.command = Message.Commands.ERROR;

            try {
                objectOutputStream.writeObject(reply);
                objectOutputStream.flush();
                objectOutputStream.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        private void processDownloadRequest(final Message message) { //client
            try {
                Socket socket = new Socket(message.device.getInetAddress(), message.device.getPortNo());
                ObjectOutputStream objectOutputStream = null;
                ObjectInputStream objectInputStream = null;

                message.socket = socket;
                message.command = Message.Commands.UPLOAD;

                objectOutputStream = new ObjectOutputStream(message.socket.getOutputStream());
                objectOutputStream.writeObject(message);
                objectOutputStream.flush();

                objectInputStream = new ObjectInputStream(socket.getInputStream());
                Object object = objectInputStream.readObject();

                if (!(object instanceof Message)) return;
                Message reply = (Message) object;
                while (reply.command != Message.Commands.DONE) {
                    switch (reply.command) {
                        case DOWNLOAD:
                            reply.string[1] = General.makeOSfriendly(reply.string[1]); // Convert to OS friendly
                            String fileToWrite = DataState.getDownloadFolder() + File.separatorChar + message.device.getMachineNameString() + File.separatorChar + reply.string[1];
                            new File(fileToWrite).mkdirs(); //make file directory
                            fileToWrite += File.separatorChar + reply.string[0];
                            message.aLong = reply.aLong;
                            message.command = Message.Commands.READY;
                            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                            objectOutputStream.writeObject(message);
                            objectOutputStream.flush();
                            downloadFile(fileToWrite, message);
                            break;
                    }

                    objectInputStream = new ObjectInputStream(socket.getInputStream());
                    reply = (Message) objectInputStream.readObject();
                }
                socket.close();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        private void downloadFile(String file, Message message) {
            try {
                Socket socket = message.socket;
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                VBox vBox = Main.Holder.getScrollVbox();
                String clientIP = socket.getInetAddress().getHostName() + ":" + socket.getPort();
                Label label = new Label("Downloading file [" + file + "] from [" + clientIP + "]");
                Platform.runLater(() -> vBox.getChildren().add(0, label));

                byte[] buffer = new byte[BLOCK_SIZE];
                int read = 0;
                long remaining = message.aLong;
                while ((read = dataInputStream.read(buffer, 0, (int) Math.min(buffer.length, remaining))) > 0) {
                    remaining -= read;
                    fileOutputStream.write(buffer, 0, read);
                }
                fileOutputStream.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private Folder getFolderFromMessage(Message message) {
            switch (message.command) {
                case GET_SHARED_FOLDER:
                    if (DataState.rootFolder == null) BackgroundTask.mineSharedFolder();
                    return DataState.rootFolder;
                case BROWSE_ROOT:
                    InetAddress address = message.socket.getInetAddress();
                    Device device = DataState.deviceList.parallelStream().filter(item -> item.getInetAddress().equals(address)).findAny().orElse(null);
                    if (device == null) return null;
                    else if (device.isFullAccess()) return DataState.sysRootFolder;
                    else if (device.isTrusted()) return DataState.rootFolder;
                    else return DataState.userFolder;
                case GET_UNSSAVED_FOLDER: // TODO working spot
                    File file = new File(message.string[0]);
                    if(!file.isDirectory()) return null;
                    Folder folder = new Folder(file, false);
                    folder.setParent(null);
                    folder.setFolderID(-1);

                    for (File entry : Objects.requireNonNull(file.listFiles())) {
                        if(entry.isDirectory()) {
                            folder.getChildren().add(new Folder(folder, entry, false));
                        } else {
                            folder.getFilesInDir().add(new IFile(
                                    entry.length(),
                                    entry.getName(),
                                    entry.getAbsolutePath(),
                                    entry.lastModified(),
                                    folder,
                                    false
                            ));
                        }
                    }

                    return folder;
            }
            return null;
        }

        private void uploadFolderInfo(Message message) {
            Folder folder = getFolderFromMessage(message);
            try {
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(message.socket.getOutputStream());
                if (folder == null) {
                    Message newMessage = new Message(Message.Commands.ERROR);
                    objectOutputStream.writeObject(newMessage);
                } else {
                    objectOutputStream.writeObject(folder);
                }
                objectOutputStream.flush();
                objectOutputStream.close();
                message.socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void uploadFile(IFile iFile, final Message message) {
            try {
                File file = new File(iFile.getAbsolutePath());
                FileInputStream fileInputStream = new FileInputStream(file);
                BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
                Socket socket = message.socket;
                ObjectOutputStream objectOutputStream = null;
                ObjectInputStream objectInputStream = null;
                OutputStream outputStream = null;

                message.command = Message.Commands.DOWNLOAD;
                if (message.string != null) {
                    if (message.string.length != 2) {
                        message.string = new String[2];
                    }
                } else {
                    message.string = new String[2];
                }

                message.string[1] = iFile.getParent().getRelativePath();
                message.string[0] = iFile.getName();
                message.aLong = iFile.getFileSize();

                objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                objectOutputStream.writeObject(message);
                objectOutputStream.flush();

                objectInputStream = new ObjectInputStream(socket.getInputStream());
                Message reply = (Message) objectInputStream.readObject();

                if (reply.command != Message.Commands.READY) return;

                outputStream = socket.getOutputStream();

                //Read IFile Contents into contents array
                byte[] contents;
                long fileLength = file.length();
                long current = 0;

                String clientIP = message.socket.getInetAddress().getHostName() + ":" + message.socket.getPort();
                VBox vBox = Main.Holder.getScrollVbox();
                Label label = new Label("Uploading file [" + message.string[0] + "] to [" + clientIP + "]");
                Platform.runLater(() -> vBox.getChildren().add(0, label));

                while (current != fileLength) {
                    int size = BLOCK_SIZE;
                    if (fileLength - current >= size)
                        current += size;
                    else {
                        size = (int) (fileLength - current);
                        current = fileLength;
                    }
                    contents = new byte[size];
                    bufferedInputStream.read(contents, 0, size);
                    outputStream.write(contents);
                }

                outputStream.flush();
                Platform.runLater(() -> label.setText("Successfully uploaded [" + message.string[0] + "] to [" + clientIP + "]"));
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        private void uploadFileFolder(final Message message) {

            if(message.aLong == -1) {

            } else {

            }

            FileFolder fileFolder = DataState.sharedFolderMap.get(message.aLong);
            IFile iFile = null;
            Folder folder = null;

            if (fileFolder.isFile()) {
                iFile = (IFile) fileFolder;
                uploadFile(iFile, message);
            } else {
                folder = (Folder) fileFolder;

                uploadFolderInfo(folder, message);
            }

            try {
                message.command = Message.Commands.DONE;
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(message.socket.getOutputStream());
                objectOutputStream.writeObject(message);
                objectOutputStream.flush();
                objectOutputStream.close();
                message.socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void uploadFolderInfo(final Folder folder, final Message message) {
            for (int i = 0; i < folder.getFilesInDir().size(); i++) {
                uploadFile(folder.getFilesInDir().get(i), message);
            }

            for (int i = 0; i < folder.getChildren().size(); i++) {
                uploadFolderInfo(folder.getChildren().get(i), message);
            }
        }

        @Override
        public void stopProcess() {
            keepRunning.set(false);
            this.interrupt();
        }

        public void addMessage(final Message message) {
            try {
                messages.transfer(message);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
