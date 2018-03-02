package networking.TCP;

import model.Stopable;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class Listener extends Thread implements Stopable {

    private static final LinkedTransferQueue<Socket> sockets = new LinkedTransferQueue<Socket>();
    private static final AtomicBoolean keepRunning = new AtomicBoolean(true);
    private ServerSocket serverSocket = null;
    private SocketProcessor socketProcessor = null;

    public static final Listener INSTANCE = new Listener();

    private Listener() {
        start();
    }

    @Override
    public void run() {

        try {
            init();
        } catch (IOException e) {
            e.printStackTrace();
            interrupt();
        }

        while (keepRunning.get()) {
            try {
                sockets.transfer(serverSocket.accept());
            } catch (IOException | InterruptedException ignore) {
            }
        }

        System.out.println("Quiting TCP Listener");
    }

    private void init() throws IOException {
        serverSocket = new ServerSocket(State.Holder.getPortNoTCP());
        socketProcessor = new SocketProcessor(sockets);
        socketProcessor.start();
    }

    @Override
    public void stopProcess() {
        sockets.clear();
        keepRunning.set(false);
        socketProcessor.stopProcess();
        this.interrupt();
    }
}
