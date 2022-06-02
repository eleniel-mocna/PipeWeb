package cz.cuni.mff.soukups3.PipeWeb;

import java.io.File;

public interface FolderTree {
    Iterable<FolderTree> folders();
    Iterable<File> files();
    File root();
}
