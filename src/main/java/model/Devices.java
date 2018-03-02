package model;

import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class Devices extends Thread implements Stopable {

    private static final LinkedTransferQueue<Device> devices = new LinkedTransferQueue<>();
    private static final AtomicBoolean keepRunning = new AtomicBoolean(true);

    public static final Devices Holder = new Devices();


    private Devices() {
        start();
    }

    public void run() {
        while (keepRunning.get()) {
            Device device = null;
            boolean found = false;
            try {
                device = devices.take();
            } catch (InterruptedException ignore) {
                continue;
            }

            if(State.hiddenDevices.contains(device.getUuid())) continue;

            for (Device existing : State.deviceList) {
                if (existing.equals(device)) {
                    found = true;
                    // Detect if name has been changed if uuid is same
                    if (!existing.getMachineName().get().equals(device.getMachineName().get())) {
                        existing.setMachineName(device.getMachineName().get());
                    }
                    existing.setLastHeardFrom(System.currentTimeMillis());
                    break;
                }
            }
            if (!found) controller.Devices.addToList(device);
        }

        System.out.println("Quiting Devices");
    }

    @Override
    public void stopProcess() {
        devices.clear();
        keepRunning.set(false);
        this.interrupt();
    }

    public static void add(Device device) {
        try {
            devices.transfer(device);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
