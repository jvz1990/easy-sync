package model;

import util.General;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class Folder implements FileFolder, Serializable {
    private static final long serialVersionUID = -9094017517798299741L;

    private List<IFile> filesInDir = null;
    private List<Folder> children = null;

    private String folderName = null;
    private long folderID;
    private Folder parent = null;

    // No save/transfer
    private transient String absolutePath = null;
    private transient File root = null;
    private transient String relativePath = null;

    // for recursion/mining
    public Folder(Folder parent, File root) {
        this.children = new LinkedList<>();
        this.filesInDir = new LinkedList<>();

        this.parent = parent;
        this.absolutePath = root.getAbsolutePath();
        this.folderName = root.getName();
        this.folderID = DataState.fileIDcounter++;

        this.root = root;

        this.relativePath = parent.getRelativePath() + File.separator + this.folderName;

        DataState.sharedFolderMap.put(this.folderID, this);
    }

    // for the absolute root
    public Folder(File root) {
        this.children = new LinkedList<>();
        this.filesInDir = new LinkedList<>();

        this.parent = null;
        this.absolutePath = root.getAbsolutePath();
        this.folderName = root.getName();
        this.folderID = DataState.fileIDcounter++;

        this.root = root;
        this.relativePath = this.folderName;

        DataState.sharedFolderMap.put(this.folderID, this);
    }

    public void mineRoot(File folder, boolean recurse) {
        if (folder.isDirectory()) {
            for (File entry : Objects.requireNonNull(folder.listFiles())) {
                if (entry.isDirectory()) {
                    Folder f = new Folder(this, entry);
                    children.add(f);
                    if (recurse) f.mineRoot(entry, recurse);
                } else {
                    filesInDir.add(new IFile(
                            entry.length(),
                            entry.getName(),
                            entry.getAbsolutePath(),
                            entry.lastModified(),
                            this
                    ));
                }
            }
        }
    }

    public String getAbsolutePath() {
        return absolutePath;
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

    public void readContent() {
        System.out.println("Folder: [" + folderName + "]");
        System.out.println("Files: ");
        for (IFile singleFile : getFilesInDir()) {
            System.out.println(singleFile.getName());
            System.out.println(singleFile.getParent().getRelativePath());
        }

        for (Folder folder : getChildren()) {
            folder.readContent();
        }
    }

    public Folder getParent() {
        return parent;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public static void main(String[] args) {
        //testA();
        Folder folder = new Folder(new File("D:\\Dropbox\\School\\18\\01\\Algorithms and Analysis\\drive-download-20180213T032039Z-001"));
        folder.mineRoot(folder.getRoot(), true);

        folder.readContent();
        for (Folder child : folder.getChildren()) {
            child.readContent();
        }

        try {
            System.out.println(General.toByteArray(folder).length);
        } catch (IOException e) {
            e.printStackTrace();
        }

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

}
