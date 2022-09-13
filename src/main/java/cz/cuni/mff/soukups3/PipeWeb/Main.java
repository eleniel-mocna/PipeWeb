package cz.cuni.mff.soukups3.PipeWeb;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        String[] command = {"/bin/bash", "-c", "/source/sample_scripts/test_script.sh" + " scripts First argument " +
                "scripts/test_script.sh Second argument scripts/test_script.sh.desc Third argument"};
        ProcessBuilder builder = new ProcessBuilder(command);
        Process p;
        try {
            p = builder.start();
            p.getOutputStream().close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
