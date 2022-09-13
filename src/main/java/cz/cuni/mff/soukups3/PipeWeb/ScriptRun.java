package cz.cuni.mff.soukups3.PipeWeb;

import java.util.Collection;

public class ScriptRun {
    /**
     * The process in which this script is run.
     */
    public final Process process;
    /**
     * The parent script which was run.
     */
    public final Script script;
    /**
     * Arguments to the script.
     */
    public final String[] arguments;
    /**
     * Was this process killed?
     */
    public boolean killed = false;

    /**
     * @param process the process in which the script is run
     * @param script the parent script
     * @param arguments the arguments with which the script was run
     */
    public ScriptRun(Process process,
                     Script script,
                     Collection<String> arguments) {
        this.process = process;
        this.script = script;
        this.arguments = arguments.toArray(new String[0]);
    }

    /**
     * Kill the process
     */
    public void kill() {
        killed = true;
        process.destroy();
    }

    public String getExitCode() {
        return process.isAlive() ? "Running" : String.valueOf(process.exitValue());
    }
}
