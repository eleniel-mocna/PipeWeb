package cz.cuni.mff.soukups3.PipeWeb;

import java.util.Collection;

public class ScriptRun {
    public final Process process;
    public final Script script;
    public final String[] arguments;
    public boolean killed=false;

    public ScriptRun(Process process,
                     Script script,
                     Collection<String> arguments){
        this.process = process;
        this.script = script;
        this.arguments = arguments.toArray(new String[0]);
    }
    public void kill(){
        killed=true;
        process.destroy();
    }
    public String getExitCode(){
        return process.isAlive() ? "Running" : String.valueOf(process.exitValue());
    }
}
