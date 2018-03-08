package model;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Folder implements FileFolder, Serializable {
    private static final long serialVersionUID = -9094017517798299741L;

    private List<IFile> filesInDir = null;
    private List<Folder> children = null;

    private String folderName = null;
    private Folder parent = null;
    private String absolutePath = null;

    // No save/transfer
    private transient File root = null;
    private transient String relativePath = null;

    // for recursion/mining
    public Folder(Folder parent, File root) {

        this.children = new ArrayList<>(getNoFoldersInFolder(root));
        this.filesInDir = new ArrayList<>(getNoFilesInFolder(root));

        this.parent = parent;
        this.absolutePath = root.getAbsolutePath();
        this.folderName = root.getName();

        this.root = root;

        this.relativePath = parent.getRelativePath() + File.separator + this.folderName;

        DataState.sharedFolderSet.add(this);
    }

    // For system roots dir
    public Folder() {
        this.children = new ArrayList<>(1);
        this.filesInDir = new ArrayList<>(0);
    }

    // for the absolute root
    public Folder(File root) {
        this.children = new ArrayList<>(getNoFoldersInFolder(root));
        this.filesInDir = new ArrayList<>(getNoFilesInFolder(root));

        this.parent = null;
        this.absolutePath = root.getAbsolutePath();
        this.folderName = root.getName();

        this.root = root;
        this.relativePath = this.folderName;

        DataState.sharedFolderSet.add(this);
    }

    public void addSystemRoots(File[] roots) {
        for (File file : roots) {
            Folder folder = new Folder(file);
            folder.setFolderName(file.getAbsolutePath());
            this.children.add(folder);
        }
    }

    public void mineRoot(File folder, boolean recurse) {
        if (folder.isDirectory()) {
            for (File entry : Objects.requireNonNull(folder.listFiles())) {
                if (entry.isDirectory()) {
                    Folder f = new Folder(this, entry);
                    this.children.add(f);
                    if (recurse) f.mineRoot(entry, recurse);
                } else {
                    this.filesInDir.add(
                            new IFile(
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

    private int getNoFoldersInFolder(File folder) {
        if(!folder.isDirectory()) return 0;

        int count = 0;
        try {
            for (File file : Objects.requireNonNull(folder.listFiles())) {
                if(file.isDirectory()) count++;
            }
        } catch (Exception ignored) {}

        return count;
    }

    private int getNoFilesInFolder(File folder) {
        if(!folder.isDirectory()) return 0;

        int count = 0;
        try{
            for (File file : Objects.requireNonNull(folder.listFiles())) {
                if (file.isFile()) count++;
            }
        } catch (Exception ignored) {}

        return count;
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

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public void setParent(Folder parent) {
        this.parent = parent;
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

    @Override
    public boolean equals(Object obj) {
        return ((Folder) obj).getAbsolutePath().equals(this.absolutePath);
    }
}
