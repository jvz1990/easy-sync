package model;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class ImageViewIcon extends ImageView{
    private String extension; // EG "png"

    public ImageViewIcon(String extension, Image image) {
        super(image);
        this.extension = extension;
    }

    public String getExtension() {
        return extension;
    }

    @Override
    public boolean equals(Object obj) {
        return ((ImageViewIcon) obj).getExtension().equals(this.extension);
    }
}
