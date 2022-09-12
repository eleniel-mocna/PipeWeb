package cz.cuni.mff.soukups3.PipeWeb;

import org.apache.commons.compress.utils.FileNameUtils;

import javax.management.openmbean.KeyAlreadyExistsException;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class Backend {
    private final static String DB_PATH = "backends.txt";
    private final static Map<String, Backend> backends;
    public final String name;
    private final String encodedPass;
    private transient ArrayList<Script> scripts;
    public ArrayList<ScriptRun> runs = new ArrayList<>();
    private File homeDir;
    private final File scriptsFolder;
    private FolderTree folderTree;

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private static String encryptPassword(String readablePassword) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] encodedHash = digest.digest(readablePassword.getBytes());
        return bytesToHex(encodedHash);
    }

    static {
        backends = new HashMap<>();
        File dbFile = new File(DB_PATH);
        if (dbFile.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(dbFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] backendInfo = line.split(";");
                    if (backendInfo.length == 4) {
                        backends.put(backendInfo[0],
                                new Backend(
                                        backendInfo[0],
                                        backendInfo[1],
                                        new File(backendInfo[2]),
                                        new File(backendInfo[3])));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private Backend(String name,
                    String encodedPass,
                    File scripts,
                    File homeDir) {
        this.name = name;
        this.encodedPass = encodedPass;
        try {
            this.homeDir = homeDir.getCanonicalFile();
        } catch (IOException e) {
            this.homeDir = homeDir;
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

    public static Collection<Backend> list() {
        return backends.values();
    }

    public static Backend forName(String name, String password) {
        Backend theBackend = backends.get(name);
        if (theBackend == null) {
            return null;
        }
        try {
            if (theBackend.encodedPass.equals(encryptPassword(password))) {
                return theBackend;
            }
        } catch (NoSuchAlgorithmException e) {
            System.err.println("SHA not found");
            throw new RuntimeException(e);
        }
        return null;
    }

    public static Backend forName(HttpSession session) {
        return backends.get((String) session.getAttribute("userName"));
    }

    public static boolean createBackend(String name, String unEncodedPass, String path) throws KeyAlreadyExistsException {
        String encodedPass;
        try {
            encodedPass = encryptPassword(unEncodedPass);
        } catch (NoSuchAlgorithmException e) {
            System.err.println("SHA not found!");
            throw new RuntimeException(e);
        }
        if (backends.get(name) == null) {
            Backend b = new Backend(name,
                    encodedPass,
                    new File(path + File.separator + ".scripts"),
                    new File(path));
            backends.put(name, b);
            saveBackends();
            return true;
        }
        return false;
    }

    public static void saveBackends() {
        StringBuffer ret = new StringBuffer();
        backends.values().forEach(x -> ret.append(x.name)
                .append(";")
                .append(x.encodedPass)
                .append(";")
                .append(x.scriptsFolder.toString())
                .append(";")
                .append(x.homeDir)
                .append(System.lineSeparator()));
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(DB_PATH))) {
            bw.write(String.valueOf(ret));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Script> getScripts() {
        return scripts;
    }

    private ArrayList<Script> loadScripts() { //TODO: test this
        ArrayList<Script> ret = new ArrayList<>();
        for (File file : Objects.requireNonNull(scriptsFolder.listFiles())) {
            if (file.getName().matches(".*\\.desc")) {
                ret.add(Script.fromConfig(new File(file.getParentFile().toString()
                                + File.separator
                                + FileNameUtils.getBaseName(file.toString())),
                        file.getAbsoluteFile()));
            }
        }
        return ret;
    }

    public boolean addScript(Script newScript, boolean hardCopy) {  //TODO: Make this nicer
        String scriptName = newScript.getPath().getName();
        for (Script s :
                scripts) {
            if (scriptName.equals(s.getPath().getName())) {
                return false;
            }
        }
        File newScriptFile = new File(scriptsFolder + File.separator + scriptName);
        File oldDescFile = new File(newScript.getPath().getPath() + ".desc");
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

        return scripts.add(newScript);
    }

    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
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

    public void reloadFolderTree() {
        folderTree = new DefaultFolderTree(homeDir);
    }

    public FolderTree getFolderTree() {
        reloadFolderTree();
        return folderTree;
    }
}
