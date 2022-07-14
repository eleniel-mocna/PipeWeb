package cz.cuni.mff.soukups3.PipeWeb.controller;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import cz.cuni.mff.soukups3.PipeWeb.DefaultFolderTree;

@Controller
public class FileTreeViewController {
    @RequestMapping(value="/fileTreeView",  method = RequestMethod.POST)
    public String fileTreeViewPost(HttpSession session, Model model,
    @RequestParam(name = "path", required = false, defaultValue = "defaultPath") String path) {
        if (!new File(path).exists()){
            path=File.listRoots()[0].toString();
        }
        try {
            model.addAttribute("fileTree", new DefaultFolderTree(new File(path).getCanonicalFile()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        model.addAttribute("path", path);
        model.addAttribute("dateFormat", new SimpleDateFormat("dd/MM/yyyy - hh:mm:ss"));
        return "fileTreeView";
    }
    @RequestMapping(value="/fileTreeView")
    public String fileTreeView(HttpSession session, Model model,
    @RequestParam(name = "path", required = false, defaultValue = "defaultPath") String path) {
        return fileTreeViewPost(session, model, path);
    }
}
