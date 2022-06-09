package cz.cuni.mff.soukups3.PipeWeb.controller;

import cz.cuni.mff.soukups3.PipeWeb.Backend;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpSession;
import java.io.File;

@Controller("/")
public class UsersController {
    @GetMapping({"/", "/index"})
    public RedirectView loginRedirect(
            HttpSession session, RedirectAttributes attributes,
            @RequestParam(name = "name", required = false, defaultValue = "") String name){
        if (!"".equals(name)){
            session.setAttribute("userName", name);
        }
        if (session.getAttribute("userName")==null) {
            return new RedirectView("/login");
        }
        else {
            return new RedirectView("/home");
        }
    }
    @GetMapping("/login")
    public String login(RedirectAttributes attributes, HttpSession session, Model model){
        model.addAttribute("logins", Backend.list().stream().map(x -> x.name).toArray(String[]::new));
        return "login";
    }

    @GetMapping("/addUser")
    public String addUser(HttpSession session, Model model,
                           @RequestParam(name = "name", required = false, defaultValue = "") String name,
                           @RequestParam(name = "scripts", required = false, defaultValue = "") String scripts_file,
                           @RequestParam(name = "home", required = false, defaultValue = "") String home_folder){
        System.err.println("INFO: addUser called with:" + name + scripts_file +home_folder);

        if ("".equals(name)){
            model.addAttribute("added", false);
            model.addAttribute("triedToAdd", false);
        }
        else {
            model.addAttribute("triedToAdd", true);
            if ("".equals(scripts_file) || "".equals(home_folder)){
                model.addAttribute("added", false);
            }
            Backend.createBackend(name, new File(scripts_file), new File(home_folder));
            model.addAttribute("added", true);
            model.addAttribute("name", name);
        }
        return ("addUser");

    }
}
