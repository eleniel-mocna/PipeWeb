package cz.cuni.mff.soukups3.PipeWeb.controller;

import cz.cuni.mff.soukups3.PipeWeb.Backend;
import cz.cuni.mff.soukups3.PipeWeb.Script;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class HomeController {
    @GetMapping("/home")
    public String home(HttpSession session, Model model,
                       @RequestParam(name = "script", required = false, defaultValue = "") String script){
        Backend backend =  Backend.forName((String) session.getAttribute("userName"));
        System.err.println("Called home from: " + session.getAttribute("userName"));
        if (!"".equals(script)){
            model.addAttribute("script",backend.getScript(script));
            backend.reloadFolderTree();
        }
        model.addAttribute("dateFormat", new SimpleDateFormat("dd/MM/yyyy - hh:mm:ss"));
        model.addAttribute("backend", backend);
        return ("home");
    }
    @GetMapping("/addScript")
    public String addScript(HttpSession session, Model model,
                            @RequestParam(name = "scriptPath", required = false, defaultValue = "") String scriptPath){
        if (!("".equals(scriptPath))){
            model.addAttribute("tried", true);
            Backend backend = Backend.forName((String) session.getAttribute("userName"));
            File descriptorFile = new File(scriptPath+".desc");
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
                //TODO:make hardCopy an input option
                backend.addScript(Script.fromConfig(scriptFile, descriptorFile), true);
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
                                 @RequestParam Map<String, String> allRequestParams){
        Backend backend = Backend.forName((String) session.getAttribute("userName"));
        backend.reloadFolderTree();
        Script script;
        if (!"".equals(scriptName)) {
            if ((script=backend.getScript(scriptName))!=null){
                backend.runs.add(script.run(backend.getFolderTree().root(), allRequestParams.keySet().stream()
                        .filter(x->!"script".equals(x))
                        .map(x -> allRequestParams.get(x)).collect(Collectors.toList())));
            }
        }
        model.addAttribute("backend", backend);
        return "runningScripts";
    }
}
