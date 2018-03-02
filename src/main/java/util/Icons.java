package util;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import model.DataState;
import model.FileFolder;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

public class Icons {

    public static void generateIcons() {
        DataState.createdIcons.clear();
        addFolderIcon();
        Iterator iterator = DataState.sharedFolderMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Long, FileFolder> entry = (Map.Entry<Long, FileFolder>) iterator.next();
            addIcon(General.getExtension(entry.getValue().getName()));
        }
    }

    public static ImageView addIcon(String extension) {
        if (DataState.createdIcons.containsKey(extension)) return null;
        Icon icon = getIcon(extension);
        if (icon == null) return null;
        BufferedImage bufferedImage = new BufferedImage(
                icon.getIconWidth(),
                icon.getIconHeight(),
                BufferedImage.TYPE_INT_ARGB
        );
        icon.paintIcon(null, bufferedImage.getGraphics(), 0, 0);
        Image fximage = SwingFXUtils.toFXImage(bufferedImage, null);
        ImageView imageView = new ImageView(fximage);
        DataState.createdIcons.put(extension, imageView);
        return imageView;
    }

    private static void addFolderIcon() {
        Icon icon = FileSystemView.getFileSystemView().getSystemIcon(new File(System.getProperty("user.dir")));
        BufferedImage bufferedImage = new BufferedImage(
                icon.getIconWidth(),
                icon.getIconHeight(),
                BufferedImage.TYPE_INT_ARGB
        );
        icon.paintIcon(null, bufferedImage.getGraphics(), 0, 0);
        Image fximage = SwingFXUtils.toFXImage(bufferedImage, null);
        ImageView imageView = new ImageView(fximage);
        DataState.createdIcons.put("fld", imageView);
    }

    private static Icon getIcon(String extension) {
        File file = null;
        try {
            file = File.createTempFile("icon", "." + extension);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return FileSystemView.getFileSystemView().getSystemIcon(file);
    }

}
