package cz.cuni.mff.soukups3.PipeWeb;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public interface FolderTree {
    Iterable<FolderTree> folders();
    Iterable<File> files();
    List<File> allFilesDirs();
    default List<File> allFilesDirs(String filter){
        System.err.println("Filter is: "+ filter);
        return allFilesDirs().stream().filter(x -> x.toString().matches(filter)).collect(Collectors.toList());
    }
    default List<File> allFilesDirs(String filter, File relativeTo){
        System.err.println("Filter is: "+ filter);
        List<File> ret = allFilesDirs().stream().filter(x -> x.toString().matches(filter)).collect(Collectors.toList());
        return ret.stream().map(x -> root().toPath().relativize(x.toPath()).toFile()).collect(Collectors.toList());
    }
    File root();
}
