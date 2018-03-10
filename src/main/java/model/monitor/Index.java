package model.monitor;

import util.General;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;

public class Index implements Serializable {
    private static final long serialVersionUID = -7477887537741865853L;
    private final HashSet<AnFile> files = new HashSet<>();

    private transient Path absoluteRoot;

    private void mineDirectory(Path path) throws IOException {

        Files.walkFileTree(path, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                File t_file = file.toFile();
                files.add(new AnFile(
                        t_file.length(),
                        t_file.getName(),
                        General.strRela(t_file.getAbsolutePath(), absoluteRoot.toString()),
                        t_file.getAbsolutePath()
                ));
                return super.visitFile(file, attrs);
            }
        });
    }

    public static void main(String[] args) {
        Index index = new Index();

        index.absoluteRoot = Paths.get("D:\\");

        try {
            index.mineDirectory(index.absoluteRoot);

            index.files.iterator().forEachRemaining(System.out::println);

        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            System.out.println(General.toByteArray(index).length);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
