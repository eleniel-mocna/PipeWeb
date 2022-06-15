package cz.cuni.mff.soukups3.PipeWeb;

import javax.validation.constraints.NotNull;
import java.io.*;
import java.util.Arrays;
import java.util.Collection;

public class Script implements Serializable {
    private final File path;
    private final char TYPE_SEPARATOR = '\t';
    private final char FILTER_SEPARATOR = ':';
    private final IOType[] inputs;
    private final IOType[] outputs;

    public void createDesc(File newDescFile) throws IOException {
        StringBuffer descContents = new StringBuffer();
        Arrays.stream(inputs).forEach(x -> descContents.append(x.name+FILTER_SEPARATOR+x.filter+TYPE_SEPARATOR));
        descContents.deleteCharAt(descContents.length()-1);
        descContents.append(System.lineSeparator());
        Arrays.stream(outputs).forEach(x -> descContents.append(x.name+FILTER_SEPARATOR+x.filter+TYPE_SEPARATOR));
        descContents.deleteCharAt(descContents.length()-1);
        BufferedWriter bw = new BufferedWriter(new FileWriter(newDescFile));
        bw.write(String.valueOf(descContents));

    }

    public static class IOType implements Serializable{
        public String name;
        public String filter;
        public IOType(String name, String filter){
            this.name = name;
            this.filter = filter.equals("") ? ".*" :filter;
        }
        public IOType(@NotNull String inputString){
            String[] splits = inputString.split(":");
            name = splits[0];
            filter = splits.length==2 ? splits[1].trim() : ".*".trim();
            filter = filter.equals("") ? ".*".trim() :filter.trim();
        }

        @Override
        public String toString() {
            return name;
        }
    }
    public Script(File path,
                  IOType[] inputs,
                  IOType[] outputs){
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
        return new Script(path, Arrays.stream(inputs).map(IOType::new).toArray(IOType[]::new),
                               Arrays.stream(outputs).map(IOType::new).toArray(IOType[]::new));
    }

    public ScriptRun run(File root, Collection<String> variables){
        System.err.println("RAN script with:" + String.join(",",
                variables.stream()
                        .map(Object::toString).toArray(String[]::new)));
        Runtime r = Runtime.getRuntime();
        Process p;
        try {
            String[] command = {"/bin/bash", "-c", path.toString() + " " + String.join(" ", variables)};
            ProcessBuilder builder = new ProcessBuilder(command);
            builder.directory(root);
            p = builder.start();
            p.getOutputStream().close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new ScriptRun(p, this, variables);
    }
    public IOType[] getInputs() {
        return inputs;
    }

    public IOType[] getOutputs() {
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
