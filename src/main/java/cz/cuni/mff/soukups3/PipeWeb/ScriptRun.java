package cz.cuni.mff.soukups3.PipeWeb;

public class ScriptRun {
    public final Process process;
    public final Script script;
    public final String[] arguments;
    public boolean killed=false;

    public ScriptRun(Process process,
                     Script script,
                     String[] arguments){
        this.process = process;
        this.script = script;
        this.arguments = arguments;
    }
    public void kill(){
        killed=true;
        process.destroy();
    }
    public String getExitCode(){
        return process.isAlive() ? "Running" : String.valueOf(process.exitValue());
    }
}
