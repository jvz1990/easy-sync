package networking.TCP;

import model.Stopable;

import java.net.Socket;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class Sender extends Thread implements Stopable {

    private static final AtomicBoolean keepRunning = new AtomicBoolean(true);
    private static final LinkedTransferQueue<Message> messages = new LinkedTransferQueue<>();

    @Override
    public void run() {
        while(keepRunning.get()) {
            Message message = null;
            Socket socket = null;
            try {
                message = messages.take();
                socket = message.socket;
            } catch (InterruptedException e) {
                continue;
            }

            switch (message.command) {
                case UPLOAD:
                    //socket.
                    break;
            }
        }
    }

    @Override
    public void stopProcess() {
        keepRunning.set(false);
        messages.clear();
        this.interrupt();
    }
}
