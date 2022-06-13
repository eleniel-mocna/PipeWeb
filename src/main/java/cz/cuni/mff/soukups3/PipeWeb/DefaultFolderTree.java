package cz.cuni.mff.soukups3.PipeWeb;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class DefaultFolderTree implements FolderTree{
    private File root;
    private List<DefaultFolderTree> subFolders;
    private List<File> files;

    public DefaultFolderTree(File root) {
        try {
            this.root = root.getCanonicalFile();
        } catch (IOException e) {
            System.err.println("IOException occurred:" + e.getStackTrace());
        }
        File[] children = root.listFiles();
        subFolders = children == null ? new LinkedList<>()
                : Arrays.stream(children).filter(File::isDirectory)
                .map(DefaultFolderTree::new)
                .collect(Collectors.toList());
        files = children == null ? new LinkedList<>()
                : Arrays.stream(children).filter(File::isFile)
                .map(File::getAbsoluteFile)
                .collect(Collectors.toList());
    }
    public DefaultFolderTree(String root){
        this(new File(root));
    }

    @Override
    public Iterable<FolderTree> folders() {
        return subFolders.stream().map(x->(FolderTree)x).collect(Collectors.toList());
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
    public List<File> allFilesDirs() {
        List<File> ret = new ArrayList<>();
        ret.add(root);
        ret.addAll(files);
        ret.addAll(subFolders.stream()
                .flatMap(x -> x.allFilesDirs().stream())
                .map(x -> new File(x.toURI().relativize(root.toURI())))
                .collect(Collectors.toList()));
        return ret;
    }

    @Override
    public String toString() {
        return root.toString();
    }
}
