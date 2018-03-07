package model;

import java.io.Serializable;

public class IFile implements FileFolder, Serializable {
    private static final long serialVersionUID = -7723410987381496076L;

    private long fileSize;
    private long fileID;
    private String fileName = null;
    private String absolutePath = null;
    private long dateModified;

    private transient Folder parent;

    public IFile(long fileSize, String fileName, String absolutePath, long dateModified, Folder parent, boolean remember) {
        this.fileSize = fileSize;
        this.fileName = fileName;
        this.absolutePath = absolutePath;
        this.dateModified = dateModified;
        this.parent = parent;
        if(remember) {
            this.fileID = DataState.fileIDcounter++;
            DataState.sharedFolderMap.put(this.fileID, this);
        } else {
            this.fileID = -1;
        }
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

    @Override
    public long getID() {
        return fileID;
    }
}
