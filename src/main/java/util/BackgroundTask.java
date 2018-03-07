package util;

import model.DataState;
import model.Folder;

import java.io.File;

public class BackgroundTask extends Thread {

    public BackgroundTask() {
        this.setPriority(Thread.MIN_PRIORITY);
        start();
    }

    public static void mineSharedFolder() {
        if(DataState.sharedFolder == null) return;
        if(DataState.sharedFolder.getValue().equals("Unnamed")) return;
        File file = new File(DataState.sharedFolder.getValue());
        if(!file.exists()) return;

        DataState.sharedFolderMap.clear();
        Folder folder = new Folder(file, true);
        folder.mineRoot(folder.getRoot(), true);
        DataState.rootFolder = folder;
    }

    public static void mineUserFolder() {
        if(DataState.userFolder != null) return;
        File file = new File(System.getProperty("user.dir"));
        Folder folder = new Folder(file, true);
        folder.mineRoot(file, false);
        DataState.userFolder = folder;
    }

    public static void createSysRootFolder() {
        DataState.sysRootFolder = new Folder(File.listRoots());
    }

    @Override
    public void run() {
        mineSharedFolder();
        mineUserFolder();
        createSysRootFolder();
        Icons.generateIcons();
        figureOS();
    }

    public static void figureOS() {
        String fullOS = System.getProperty("os.name");
        if(fullOS.toLowerCase().contains("windows")) General.OS = General.OSs.WINDOWS;
        else General.OS = General.OSs.OTHER;
    }
}
