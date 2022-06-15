package cz.cuni.mff.soukups3.PipeWeb;

import org.apache.commons.compress.utils.FileNameUtils;

import javax.management.openmbean.KeyAlreadyExistsException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class Backend {
    private final static String DB_PATH = "backends.txt";
    private final static Map<String, Backend> backends;
    public final String name;
    private transient ArrayList<Script> scripts;
    public ArrayList<ScriptRun> runs = new ArrayList<>();
    private File homeDir;
    private final File scriptsFolder;
    private FolderTree folderTree;

    static {
        backends = new HashMap<>();
        File dbFile = new File(DB_PATH);
        if (dbFile.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(dbFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] backendInfo = line.split(";");
                    if (backendInfo.length == 3) {
                        backends.put(backendInfo[0],
                                new Backend(
                                        backendInfo[0],
                                        new File(backendInfo[1]),
                                        new File(backendInfo[2])));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
    public Backend(String name,
                   File scripts,
                   File homeDir){
        this.name = name;
        try {
            this.homeDir = homeDir.getCanonicalFile();
        } catch (IOException e) {
            this.homeDir=homeDir;
        }
        this.homeDir.mkdir();
        scriptsFolder = new File(homeDir + File.separator + "scripts");
        scriptsFolder.mkdir();
        reloadFolderTree();
        if (homeDir.mkdir()) {
            System.err.println("INFO: Created directory" + homeDir); // TODO: Add a logger
        }
        this.scripts = loadScripts();
    }
    public static Collection<Backend> list(){
        return backends.values();
    }
    public static Backend forName(String name){
        return backends.get(name);
    }

    public static boolean createBackend(String name) throws KeyAlreadyExistsException {
        if (backends.get(name)==null){
            Backend b = new Backend(name,
                    new File(name+File.separator+".scripts"),
                    new File(name));
            backends.put(name, b);
            return true;
        }
        return false;
    }
//    public static void saveBackends(){
//        StringBuffer ret = new StringBuffer();
//        backends.values().forEach(x -> ret.append(x.name+";"+x.scriptsFolder.toString()+";"+x.homeDir+System.lineSeparator()));
//        try(BufferedWriter bw = new BufferedWriter(new FileWriter(DB_PATH))) {
//            bw.write(String.valueOf(ret));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    public ArrayList<Script> getScripts() {
        return scripts;
    }

    private ArrayList<Script> loadScripts(){ //TODO: test this
        ArrayList<Script> ret = new ArrayList<>();
        for (File file : Objects.requireNonNull(scriptsFolder.listFiles())){
            if (file.getName().matches(".*\\.desc")){
                ret.add(Script.fromConfig(new File(file.getParentFile().toString()
                                + File.separator
                                + FileNameUtils.getBaseName(file.toString())),
                        file.getAbsoluteFile()));
            }
        }
        return ret;
    }
//    private boolean saveScripts(boolean overwrite){
//        ArrayList<Script> old_scripts = loadScripts();
//        if (!overwrite && old_scripts.size() >scripts.size()){
//            return false;
//        }
//        try(ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(scripts_file))) {
//            os.writeObject(scripts);
//            return true;
//        } catch (IOException e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
    public boolean addScript(Script newScript, boolean hardCopy) {  //TODO: Make this nicer
        String scriptName = newScript.getPath().getName();
        for (Script s :
                scripts) {
            if (scriptName.equals(s.getPath().getName())){
                return false;
            }
        }
        File newScriptFile = new File(scriptsFolder + File.separator + scriptName);
        File oldDescFile = new File(newScript.getPath().getPath()+".desc");
        File newDescFile = new File(scriptsFolder + File.separator + scriptName + ".desc");
        try {
            if (hardCopy) {
                Files.copy(newScript.getPath().toPath(), newScriptFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                if (oldDescFile.exists()) {
                    Files.copy(oldDescFile.toPath(), newDescFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } else {
                    newScript.createDesc(newDescFile);
                }
            } else {
                Files.createLink(newScriptFile.toPath(), newScript.getPath().toPath());
                if (oldDescFile.exists()) {
                    Files.createLink(newDescFile.toPath(), oldDescFile.toPath());
                } else {
                    newScript.createDesc(newDescFile);
                }
            }
        } catch (IOException e) {
            System.err.println("Could not create scripts, trying to delete what was created...");
            e.printStackTrace();
            try {
                Files.deleteIfExists(newScriptFile.toPath());
                Files.deleteIfExists(newDescFile.toPath());
                System.err.println("Files deleted successfully");
            } catch (IOException ex) {
                ex.printStackTrace();
                System.err.println("Files might not have been deleted successfully!");
            }
        }

        boolean ret = scripts.add(newScript);
        return ret;
    }
    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException{
        in.defaultReadObject();
        this.scripts = loadScripts();
    }
    public Script getScript(String name) {
        for (Script s :
                scripts) {
            if (s.toString().equals(name)) {
                return s;
            }
        }
        return null;
    }

    public void reloadFolderTree(){
        folderTree = new DefaultFolderTree(homeDir);
    }
    public FolderTree getFolderTree(){
        return folderTree;
    }
}
