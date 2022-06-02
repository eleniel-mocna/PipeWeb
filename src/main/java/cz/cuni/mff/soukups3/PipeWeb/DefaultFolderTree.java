package cz.cuni.mff.soukups3.PipeWeb;

import com.fasterxml.jackson.databind.util.ArrayIterator;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

public class DefaultFolderTree implements FolderTree{
    private File root;
    private Iterable<FolderTree> subFolders;
    private Iterable<File> files;

    public DefaultFolderTree(File root) {
        try {
            this.root = root.getCanonicalFile();
        } catch (IOException e) {
            System.err.println("IOException occurred:" + e.getStackTrace());
        }
        File[] children = root.listFiles();
        subFolders = children == null ? new ArrayIterator<FolderTree>(null)
                : Arrays.stream(children).filter(File::isDirectory).map(DefaultFolderTree::new)
                .collect(Collectors.toList());
        files = children == null ? new ArrayIterator<File>(null)
                : Arrays.stream(children).filter(File::isFile).collect(Collectors.toList());
    }
    public DefaultFolderTree(String root){
        this(new File(root));
    }

    @Override
    public Iterable<FolderTree> folders() {
        return subFolders;
    }

    @Override
    public Iterable<File> files() {
        return files;
    }

    @Override
    public File root() {
        return root;
    }
    @Override
    public String toString() {
        return root.toString();
    }
}
