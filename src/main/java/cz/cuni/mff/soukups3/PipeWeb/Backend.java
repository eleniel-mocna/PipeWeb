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

/**
 * This is a class describing a users backend with all the info that is needed.
 * <p>
 * This class is a multiton which can be accessed by 2 ways. Either by giving a name and a password
 * or by giving an HTMLSession with a name already present.
 */
public class Backend {
    /**
     * Path to the database of available backends.
     */
    private final static String DB_PATH = "backends.txt";
    /**
     * Multiton map for instances
     */
    private final static Map<String, Backend> backends = new HashMap<>();
    /**
     * Name of the user
     */
    public final String name;
    /**
     * SHA256 encoded password
     */
    private final String encodedPass;
    /**
     * List of all script files. These are reloaded every time from disk when the instance is created
     */
    private transient ArrayList<Script> scripts;
    /**
     * All runs of scripts in this session.
     */
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
                    File ignoredScripts, //Always homeDir/scripts, this is for
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
            System.err.println("INFO: Created directory" + homeDir);
        }
        this.scripts = loadScripts();
    }

    /**
     * Get all available backends.
     * @return All available backends.
     */
    public static Collection<Backend> list() {
        return backends.values();
    }

    /**
     * Get a Backend for a name with given password, or null if incorrect
     * @param name userName
     * @param password unencrypted password
     * @return wanted Backend or null
     */
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

    /**
     * @apiNote This should be removed and logging in should be supported just by name and password
     * (you can set the name in the session without the correct password, but since this webpage should be run
     * in a secure network where all users aren't malicious, this is not a big issue)
     * @param session A session with a userName set
     * @return wanted Backend
     */
    public static Backend forName(HttpSession session) {
        return backends.get((String) session.getAttribute("userName"));
    }

    /**
     * Create a backend with the given properties
     * @param name the userName
     * @param unEncodedPass password as entered by the user
     * @param path path to the root directory
     * @throws KeyAlreadyExistsException if a username already exists.
     */
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

    /**
     * Save backends to the DB_PATH file.
     */
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

    /**
     * Add a script to the backend either by hard-linking to the scripts folder or by copying it there
     * @param newScript the script to be added
     * @param hardCopy if true copy to the folder  else hard-link
     */
    public void addScript(Script newScript, boolean hardCopy) {  //TODO: Make this nicer
        String scriptName = newScript.getPath().getName();
        for (Script s :
                scripts) {
            if (scriptName.equals(s.getPath().getName())) {
                return;
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

        scripts.add(newScript);
    }

    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.scripts = loadScripts();
    }

    /**
     * @param name name of the wanted script
     * @return The wanted script or null if it doesn't exist
     */
    public Script getScript(String name) {
        for (Script s :
                scripts) {
            if (s.toString().equals(name)) {
                return s;
            }
        }
        return null;
    }

    /**
     * Reload the folder tree from the root.
     */
    public void reloadFolderTree() {
        folderTree = new DefaultFolderTree(homeDir);
    }

    /**
     * @return Folder tree from the root.
     */
    public FolderTree getFolderTree() {
        reloadFolderTree();
        return folderTree;
    }
}
