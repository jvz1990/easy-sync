package model;

import java.io.Serializable;

public class IFile implements FileFolder, Serializable {
    private static final long serialVersionUID = -7723410987381496076L;

    private long fileSize;
    private String fileName = null;
    private String absolutePath = null;
    private long dateModified;

    private transient Folder parent;

    public IFile(long fileSize, String fileName, String absolutePath, long dateModified, Folder parent) {
        this.fileSize = fileSize;
        this.fileName = fileName;
        this.absolutePath = absolutePath;
        this.dateModified = dateModified;
        this.parent = parent;
        DataState.sharedFolderSet.add(this);
    }

    public long getDateModified() {
        return dateModified;
    }

    public long getFileSize() {
        return fileSize;
    }

    public Folder getParent() {
        return parent;
    }

    @Override
    public boolean equals(Object obj) {
        return ((IFile) obj).getAbsolutePath().equals(this.absolutePath);
    }

    @Override
    public String getAbsolutePath() {
        return absolutePath;
    }

    @Override
    public boolean isFile() {
        return true;
    }

    @Override
    public String getName() {
        return fileName;
    }

}
