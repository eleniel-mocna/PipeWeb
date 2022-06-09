package cz.cuni.mff.soukups3.PipeWeb.controller;

import cz.cuni.mff.soukups3.PipeWeb.Backend;
import cz.cuni.mff.soukups3.PipeWeb.Script;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.io.File;

@Controller
public class HomeController {
    @GetMapping("/home")
    public String home(HttpSession session, Model model,
                       @RequestParam(name = "script", required = false, defaultValue = "") String script){
        Backend backend =  Backend.forName((String) session.getAttribute("userName"));
        System.err.println("Called home from: " + session.getAttribute("userName"));
        if (!"".equals(script)){
            model.addAttribute("script",backend.getScript(script));
        }
        model.addAttribute("backend", backend);
        return ("home");
    }
    @GetMapping("/addScript")
    public String addScript(HttpSession session, Model model,
                            @RequestParam(name = "scriptPath", required = false, defaultValue = "") String scriptPath,
                            @RequestParam(name = "descriptorPath", required = false, defaultValue = "") String descriptorPath){
        if (!("".equals(scriptPath) || "".equals(descriptorPath))){
            model.addAttribute("tried", true);
            Backend backend = Backend.forName((String) session.getAttribute("userName"));
            File descriptorFile = new File(descriptorPath);
            File scriptFile = new File(scriptPath);
            boolean failed = false;
            if (!scriptFile.exists()){
                failed = true;
                model.addAttribute("scriptExists", false);
            } else model.addAttribute("scriptExists", true);
            if (!descriptorFile.exists()){
                failed = true;
                model.addAttribute("descriptorExists", false);
            } else model.addAttribute("descriptorExists", true);

            if (!failed) {
                backend.addScript(Script.fromConfig(scriptFile, descriptorFile));
            }
            model.addAttribute("failed", failed);
        }
        else {
            model.addAttribute("tried", false);
        }
        return "addScript";
    }
    @GetMapping("/runningScripts")
    public String runningScripts(HttpSession session, Model model,
                            @RequestParam(name = "script", required = false, defaultValue = "") String scriptName,
                            @RequestParam(name = "arguments", required = false, defaultValue = "") String arguments){
        Backend backend = Backend.forName((String) session.getAttribute("userName"));
        Script script;
        if (!"".equals(scriptName)) {
            if ((script=backend.getScript(scriptName))!=null){
                backend.runs.add(script.run(new String[]{arguments}));
            }
        }
        model.addAttribute("backend", backend);
        return "runningScripts";
    }
}
