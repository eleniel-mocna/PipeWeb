package cz.cuni.mff.soukups3.PipeWeb;

import javax.validation.constraints.NotNull;
import java.io.*;
import java.util.Arrays;
import java.util.Collection;

/**
 * Object of this class represents a script, which then can be managed and run from
 * this object.
 */
public class Script implements Serializable {
    private final File path;
    private final char TYPE_SEPARATOR = '\t';
    private final char FILTER_SEPARATOR = ':';
    private final IOType[] inputs;
    private final IOType[] outputs;

    /**
     * Create a description file for this script.
     *
     * @param newDescFile file to which save the description
     * @throws IOException if IOException is thrown by file system
     */
    public void createDesc(File newDescFile) throws IOException {
        StringBuffer descContents = new StringBuffer();
        Arrays.stream(inputs).forEach(x -> descContents
                .append(x.name)
                .append(FILTER_SEPARATOR)
                .append(x.filter)
                .append(TYPE_SEPARATOR));
        descContents.deleteCharAt(descContents.length() - 1);
        descContents.append(System.lineSeparator());
        Arrays.stream(outputs).forEach(x -> descContents
                .append(x.name)
                .append(FILTER_SEPARATOR)
                .append(x.filter)
                .append(TYPE_SEPARATOR));
        descContents.deleteCharAt(descContents.length() - 1);
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(newDescFile));
        bufferedWriter.write(String.valueOf(descContents));

    }

    /**
     * Record of this class represents an input or output into a script.
     */
    public static class IOType implements Serializable {
        /**
         * The user readable name of the input/output.
         */
        public final String name;
        /**
         * Regex filter matching all possible files that should be put as this argument.
         */
        public final String filter;

        /**
         * @param name   user readable name of the input/output
         * @param filter regex filter matching all possible files that should be put as this argument
         */
        public IOType(String name, String filter) {
            this.name = name;
            this.filter = filter.equals("") ? ".*" : filter;
        }

        /**
         * Generate an IOType from a line in the script description file.
         *
         * @param inputString line from a script description file
         */
        public IOType(@NotNull String inputString) {
            String[] splits = inputString.split(":");
            name = splits[0];
            String temp_filter = splits.length == 2 ? splits[1].trim() : ".*".trim();
            filter = temp_filter.equals("") ? ".*".trim() : temp_filter.trim();
        }

        @Override
        public String toString() {
            return "[" + name + "]";
        }
    }

    /**
     * Create a script object with thefollowing parameters
     * @param path the path to the script
     * @param inputs inputs to the script
     * @param outputs outputs to the script
     */
    public Script(File path,
                  IOType[] inputs,
                  IOType[] outputs) {
        this.path = path.getAbsoluteFile();
        this.inputs = inputs;
        this.outputs = outputs;
    }

    /**
     * Get a script from the script file and the script descriptor file.
     * @param script_file the script file
     * @param desc_file the script descriptor file
     * @return
     */
    public static Script fromConfig(File script_file,
                                    File desc_file) {
        String[] inputs = null;
        String[] outputs = null;
        try (BufferedReader br = new BufferedReader(new FileReader(desc_file))) {
            inputs = br.readLine().split("\t");
            outputs = br.readLine().split("\t");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new Script(script_file, Arrays.stream(inputs).map(IOType::new).toArray(IOType[]::new),
                Arrays.stream(outputs).map(IOType::new).toArray(IOType[]::new));
    }

    /**
     * Run the script in the `root` folder with the given variables
     * @param root path at which to run the script
     * @param variables all variables to be submitted to the script as variables
     * @return object collecting the result
     */
    public ScriptRun run (File root, Collection<String> variables) throws IOException{
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
