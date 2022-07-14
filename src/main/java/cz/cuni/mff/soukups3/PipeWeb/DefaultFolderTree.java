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
    
    public DefaultFolderTree(File root, int layers) {
        try {
            this.root = root.getCanonicalFile();
            System.err.println(root + " was loaded!");
            System.err.println(root.lastModified() + " modified.");
        } catch (IOException e) {
            System.err.println("IOException occurred:" + e.getStackTrace());
        }
        File[] children = root.listFiles();
        files = children == null ? new LinkedList<>()
            : Arrays.stream(children).filter(File::isFile)
            .map(File::getAbsoluteFile)
            .collect(Collectors.toList());
        if (layers!=0){                        
            subFolders = children == null ? new LinkedList<>()
                    : Arrays.stream(children).filter(File::isDirectory)
                    .map(t -> new DefaultFolderTree(t,layers-1))
                    .collect(Collectors.toList());
        } else {
            subFolders = new LinkedList<>();
        }
    }
    public DefaultFolderTree(String root, int layers){
        this(new File(root), layers);
    }
    public DefaultFolderTree(File root){
        this(root, 1);
    }
    public DefaultFolderTree(String root){
        this(root, 1);
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
                .collect(Collectors.toList()));
        return ret;
    }

    @Override
    public String toString() {
        return root.toString();
    }
}
