package cz.cuni.mff.soukups3.PipeWeb;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public interface FolderTree {
    Iterable<FolderTree> folders();
    Iterable<File> files();
    List<File> allFilesDirs();
    default List<File> allFilesDirs(String filter){
        return allFilesDirs().stream().filter(x -> x.toString().matches(filter)).collect(Collectors.toList());
    }
    File root();
}
