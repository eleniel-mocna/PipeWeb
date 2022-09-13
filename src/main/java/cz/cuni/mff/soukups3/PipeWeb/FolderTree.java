package cz.cuni.mff.soukups3.PipeWeb;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Objects of this interface represent a filesystem tree. These then can be used
 * to generate a fileManager in the frontend.
 */
public interface FolderTree {
    /**
     * @return all folders in the root directory of this folderTree
     */
    Iterable<FolderTree> folders();

    /**
     * @return all files in the root directory of this folderTree
     */
    Iterable<File> files();

    /**
     * @return all files and directories in the root directory of this folderTree
     */
    List<File> allFilesDirs();

    /**
     * @param filter regex which all returned files and directories must match
     * @return all files and directories in the root directory of this folderTree
     *   that match the filter regex
     */
    default List<File> allFilesDirs(String filter){
        return allFilesDirs().stream().filter(x -> x.toString().matches(filter)).collect(Collectors.toList());
    }

    /**
     * @param filter regex which all returned files and directories must match
     * @param relativeTo file to which all files should be relative to
     * @return all files and dirs filtered and relative to the given file
     */
    default List<File> allFilesDirs(String filter, File relativeTo){
        List<File> ret = allFilesDirs().stream().filter(x -> x.toString().matches(filter)).collect(Collectors.toList());
        return ret.stream().map(x -> root().toPath().relativize(x.toPath()).toFile()).collect(Collectors.toList());
    }

    /**
     * @return root folder of this folderTree
     */
    File root();

    /**
     * Reload all changes in the folderTree from the file system.
     */
    void refresh();
}
