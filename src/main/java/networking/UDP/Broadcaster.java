package networking.UDP;

import model.DataState;
import model.Stopable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicBoolean;

public class Broadcaster extends Thread implements Stopable {

    private static final int PORT_NO_UDP = 12226;
    private static final int BROADCAST_SLEEP = 5000;
    private static final AtomicBoolean keepRunning = new AtomicBoolean(true);

    private Broadcaster() {
        start();
    }

    @Override
    public void run() {

        System.out.println("started");

        DatagramSocket datagramSocket;
        try {
            datagramSocket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
            return;
        }

        ArrayList<DatagramPacket> datagramPackets = init();
        if (datagramPackets == null) return;

        while (keepRunning.get()) {
            try {
                for (DatagramPacket datagramPacket : datagramPackets) {
                    datagramSocket.send(datagramPacket);
                }
                Thread.sleep(BROADCAST_SLEEP);

            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }

        datagramSocket.close();
    }

    private ArrayList<DatagramPacket> init() {
        ArrayList<DatagramPacket> datagramPackets = new ArrayList<>();
        byte[] packet;

        try {
            // make packet
            BroadCastingPacket broadCastingPacket = new BroadCastingPacket(
                    DataState.getMachineName(),
                    DataState.Holder.getPortNoTCP(),
                    DataState.Holder.getUuid());

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);

            objectOutputStream.writeObject(broadCastingPacket);
            objectOutputStream.flush();
            objectOutputStream.close();

            packet = byteArrayOutputStream.toByteArray();

            //Create datagrams for all net networks on NIC
            datagramPackets.add(new DatagramPacket(packet, packet.length, InetAddress.getByName("255.255.255.255"), PORT_NO_UDP));

            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();

                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }

                for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                    InetAddress inetAddress = interfaceAddress.getBroadcast();
                    if (inetAddress == null) {
                        continue;
                    }
                    datagramPackets.add(new DatagramPacket(packet, packet.length, inetAddress, PORT_NO_UDP));
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return datagramPackets;
    }

    @Override
    public void stopProcess() {
        keepRunning.set(false);
    }

    public void Restart() {
        new Thread(() -> {
            keepRunning.set(false);
            while (Holder.INSTANCE.isAlive()) {
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            keepRunning.set(true);
            Holder.INSTANCE.run();
        }).start();
    }

    public static class Holder {
        public static final Broadcaster INSTANCE = new Broadcaster();
    }

}
