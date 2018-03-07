package model;

import util.General;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class Folder implements model.FileFolder, Serializable {
    private static final long serialVersionUID = -9094017517798299741L;

    private List<IFile> filesInDir = null;
    private List<Folder> children = null;

    private String folderName = null;
    private long folderID;
    private Folder parent = null;
    private String absolutePath = null;

    // No save/transfer
    private transient File root = null;
    private transient String relativePath = null;

    // for recursion/mining
    public Folder(Folder parent, File root, boolean remember) {
        this.children = new LinkedList<>();
        this.filesInDir = new LinkedList<>();

        this.parent = parent;
        this.absolutePath = root.getAbsolutePath();
        this.folderName = root.getName();

        this.root = root;

        this.relativePath = parent.getRelativePath() + File.separator + this.folderName;

        if(remember) {
            this.folderID = DataState.fileIDcounter++;
            DataState.sharedFolderMap.put(this.folderID, this);
        } else {
            this.folderID = -1;
        }

    }

    // For system root dir
    public Folder(File[] roots) {
        this.children = new LinkedList<>();

        for (File file : roots) {
            Folder folder = new Folder(file, false);
            folder.setFolderName(file.getAbsolutePath());
            folder.setAbsolutePath(file.getAbsolutePath());
            folder.setParent(this);
            this.children.add(folder);
        }

        this.parent = null;
        this.filesInDir = new LinkedList<>();

    }

    // for the absolute root
    public Folder(File root, boolean remember) {
        this.children = new LinkedList<>();
        this.filesInDir = new LinkedList<>();

        this.parent = null;
        this.absolutePath = root.getAbsolutePath();
        this.folderName = root.getName();

        this.root = root;
        this.relativePath = this.folderName;

        if(remember) {
            this.folderID = DataState.fileIDcounter++;
            DataState.sharedFolderMap.put(this.folderID, this);
        } else {
            this.folderID = -1;
        }
    }

    public void mineRoot(File folder, boolean recurse) {
        if (folder.isDirectory()) {
            for (File entry : Objects.requireNonNull(folder.listFiles())) {
                if (entry.isDirectory()) {
                    Folder f = new Folder(this, entry, true);
                    this.children.add(f);
                    if (recurse) f.mineRoot(entry, recurse);
                } else {
                    this.filesInDir.add(new IFile(
                            entry.length(),
                            entry.getName(),
                            entry.getAbsolutePath(),
                            entry.lastModified(),
                            this,
                            true
                    ));
                }
            }
        }
    }

    public List<Folder> getChildren() {
        return children;
    }

    public File getRoot() {
        return root;
    }

    public List<IFile> getFilesInDir() {
        return filesInDir;
    }

    public Folder getParent() {
        return parent;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public static void main(String[] args) {
        //testA();
        Folder folder = new Folder(new File("D:\\Dropbox\\School\\18\\01\\Algorithms and Analysis\\drive-download-20180213T032039Z-001"), true);
        folder.mineRoot(folder.getRoot(), true);

        System.out.println(folder);

        try {
            System.out.println(General.toByteArray(folder).length);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public void setParent(Folder parent) {
        this.parent = parent;
    }

    public void setFolderID(long folderID) {
        this.folderID = folderID;
    }

    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath;
    }

    public void setAbsolutePath(String absolutePath) {
        this.absolutePath = absolutePath;
    }

    @Override
    public String getAbsolutePath() {
        return absolutePath;
    }

    @Override
    public boolean isFile() {
        return false;
    }

    @Override
    public String getName() {
        return folderName;
    }

    @Override
    public long getID() {
        return folderID;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("Folder: [").append(folderName).append("]");

        for (IFile iFile : filesInDir) {
            stringBuilder.append("\n File: [").append(iFile.getAbsolutePath()).append(iFile.getName()).append("]");
        }

        for (Folder child : children) {
            stringBuilder.append('\n').append(child.toString());
        }

        return stringBuilder.toString();

    }
}
