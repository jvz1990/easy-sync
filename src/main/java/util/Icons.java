package util;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import model.DataState;
import model.ImageViewIcon;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Icons {

    public static void generateIcons() {
        DataState.createdIcons.clear();
        addFolderIcon();
        DataState.sharedFolderSet.forEach(fileFolder -> addIcon(General.getExtension(fileFolder.getName())));
    }

    public static ImageViewIcon addIcon(String extension) {
        Icon icon = getIcon(extension);
        if (icon == null) return null;
        BufferedImage bufferedImage = new BufferedImage(
                icon.getIconWidth(),
                icon.getIconHeight(),
                BufferedImage.TYPE_INT_ARGB
        );
        icon.paintIcon(null, bufferedImage.getGraphics(), 0, 0);
        Image fximage = SwingFXUtils.toFXImage(bufferedImage, null);
        ImageViewIcon imageViewIcon = new ImageViewIcon(extension, fximage);
        DataState.createdIcons.add(imageViewIcon);
        return imageViewIcon;
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
        DataState.createdIcons.add(new ImageViewIcon("fld", fximage));
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
