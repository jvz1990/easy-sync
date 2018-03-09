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
        if(obj == null) return false;
        return this == obj || this.getClass() == obj.getClass() && ((ImageViewIcon) obj).getExtension().equals(this.extension);
    }

    @Override
    public int hashCode() {
        return extension.hashCode();
    }
}
