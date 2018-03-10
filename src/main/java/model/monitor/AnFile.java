package model.monitor;

import java.io.Serializable;

public class AnFile implements Serializable {
    private static final long serialVersionUID = -7130854422657214137L;
    private long size;
    private String fileName;
    private String relativePath;

    private transient String absolutePath;

    public AnFile(long size, String fileName, String relativePath, String absolutePath) {
        this.size = size;
        this.fileName = fileName;
        this.relativePath = relativePath;
        this.absolutePath = absolutePath;
    }

    public long getSize() {
        return size;
    }

    public String getFileName() {
        return fileName;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public String getAbsolutePath() {
        return absolutePath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AnFile anFile = (AnFile) o;

        return size == anFile.size && fileName.equals(anFile.fileName);
    }

    @Override
    public int hashCode() {
        return 31 * ((int) (size ^ (size >>> 32))) + fileName.hashCode();
    }

    @Override
    public String toString() {
        return relativePath;
    }

}
