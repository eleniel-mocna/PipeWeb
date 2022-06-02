package cz.cuni.mff.soukups3.PipeWeb.controller;

import cz.cuni.mff.soukups3.PipeWeb.DefaultFolderTree;
import cz.cuni.mff.soukups3.PipeWeb.FolderTree;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

@Controller
public class WelcomeController {

    // inject via application.properties
    @Value("${welcome.message}")
    private String message;

    @GetMapping("/list")
    public String list(
            @RequestParam(name = "name", required = false, defaultValue = "/Users/Public") String name, Model model){
        File dir;
        model.addAttribute("message", message);
        try {
            dir = new File(name).getCanonicalFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        FolderTree files = new DefaultFolderTree(dir);
        FolderTree[] tasks=Arrays.stream(Objects.requireNonNull(dir.listFiles()))
                .filter(File::isDirectory).map(DefaultFolderTree::new).toArray(DefaultFolderTree[]::new);
        if (tasks.length<1){
            tasks = new FolderTree[1];
            try {
                tasks[0] = new DefaultFolderTree(dir.getCanonicalPath() + "\\.");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
        model.addAttribute("tasks", tasks);
        return "welcome";
    }

}