package cz.cuni.mff.soukups3.PipeWeb;

import java.io.*;
import java.util.Arrays;

public class Script implements Serializable {
    private final File path;
    private final String[] inputs;
    private final String[] outputs;

    public Script(File path,
                  String[] inputs,
                  String[] outputs){
        this.path = path.getAbsoluteFile();
        this.inputs = inputs;
        this.outputs = outputs;
    }
    public static Script fromConfig(File path,
                                    File config){
        String[] inputs = null;
        String[] outputs = null;
        try (BufferedReader br = new BufferedReader(new FileReader(config))){
            inputs = br.readLine().split("\t");
            outputs = br.readLine().split("\t");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new Script(path, inputs, outputs);
    }

    public ScriptRun run(String[] variables){
        System.err.println("RAN script with:" + String.join(",", Arrays.stream(variables).map(Object::toString).toArray(String[]::new)));
        Runtime r = Runtime.getRuntime();
        Process p;
        try {
            p = r.exec(path + " " + String.join(" ", variables));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new ScriptRun(p, this, variables);
    }
    public String[] getInputs() {
        return inputs;
    }

    public String[] getOutputs() {
        return outputs;
    }

    public File getPath() {
        return path;
    }

    @Override
    public String toString() {
        return path.toString();
    }
}
