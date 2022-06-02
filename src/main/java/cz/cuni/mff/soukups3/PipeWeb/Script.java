package cz.cuni.mff.soukups3.PipeWeb;

import java.io.File;
import java.io.IOException;

public class Script {
    private File path;

    public Script(File path){
        this.path = path;
    }

    public boolean run(String[] variables){
        Runtime r = Runtime.getRuntime();
        Process p;
        try {
            p = r.exec(path + String.join(" ", variables));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return false;
    }
    public String[] getInputs() {
        return new String[0];
    }

    public String[] getOutputs() {

        return new String[0];
    }
}
