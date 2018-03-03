package networking.UDP;

import model.DataState;
import model.Device;
import model.Devices;
import model.Stopable;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicBoolean;

public class Listener extends Thread implements Stopable {

    private static final int PORT_NO_UDP = 12226;
    private static final AtomicBoolean keepRunning = new AtomicBoolean(true);
    private DatagramSocket datagramSocket = null;

    private Listener() {
        start();
    }

    @Override
    public void run() {

        byte[] rawPacket = new byte[1024];
        DatagramPacket datagramPacket = new DatagramPacket(rawPacket, rawPacket.length);
        ObjectInputStream objectInputStream = null;
        ByteArrayInputStream byteArrayInputStream = null;

        try {
            datagramSocket = new DatagramSocket(PORT_NO_UDP, InetAddress.getByName("0.0.0.0"));
            datagramSocket.setBroadcast(true);
        } catch (IOException e) {
            keepRunning.set(false);
            e.printStackTrace();
            interrupt();
        }

        while (keepRunning.get()) {
            try {
                datagramSocket.receive(datagramPacket);

                if (byteArrayInputStream == null) {
                    byteArrayInputStream = new ByteArrayInputStream(rawPacket);
                } else {
                    byteArrayInputStream.reset();
                }

                objectInputStream = new ObjectInputStream(new BufferedInputStream(byteArrayInputStream));

                BroadCastingPacket receivedPacket = (BroadCastingPacket) objectInputStream.readObject();

                objectInputStream.close();
                objectInputStream = null;

                if (receivedPacket.getMachineUUID().compareTo(DataState.Holder.getUuid()) != 0) {
                    Devices.add(
                            new Device(
                                    datagramPacket.getAddress(),
                                    receivedPacket.getTCPport(),
                                    receivedPacket.getMachineName(),
                                    receivedPacket.getMachineUUID(),
                                    false,
                                    false,
                                    true
                            ));
                }

            } catch (IOException | ClassNotFoundException ignore) {
            }

        }

        if (objectInputStream != null) try {
            objectInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        datagramSocket.close();

        System.out.println("Quitting UDP Listener");
    }

    @Override
    public void stopProcess() {
        datagramSocket.close();
        keepRunning.set(false);
        interrupt();
    }

    public static final class Holder {
        public static final Listener INSTANCE = new Listener();
    }
}
