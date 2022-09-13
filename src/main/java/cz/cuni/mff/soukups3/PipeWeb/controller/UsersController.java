package cz.cuni.mff.soukups3.PipeWeb.controller;

import cz.cuni.mff.soukups3.PipeWeb.Backend;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpSession;

/**
 * This controller takes care of user management (logging in, authorization, adding users etc.)
 */
@Controller("/")
public class UsersController {
    @RequestMapping({"/"})
    public RedirectView loginGetRedirect(HttpSession session, RedirectAttributes attributes){
        if (Backend.forName(session)==null) {
            return new RedirectView("/login");
        }
        return new RedirectView("/home");
    }
    @RequestMapping(value = "/index", method = RequestMethod.POST)
    public RedirectView loginRedirect(
            HttpSession session, RedirectAttributes attributes,
            @RequestParam(name = "name", required = false, defaultValue = "") String name,
            @RequestParam(name = "pass", required = false, defaultValue = "") String password) {
        Backend backend = Backend.forName(name, password);
        if (backend != null) {
            session.setAttribute("userName", name);
            return new RedirectView("/home");
        }
        attributes.addAttribute("failedLogin", true);
        return new RedirectView("/login");

    }

    @GetMapping("/login")
    public String login(RedirectAttributes attributes, HttpSession session, Model model,
                        @RequestParam(value = "failedLogin", required = false, defaultValue = "false") String failedLogin) {
        model.addAttribute("failedLogin", failedLogin);
        model.addAttribute("logins", Backend.list().stream().map(x -> x.name).toArray(String[]::new));
        return "login";
    }

    @RequestMapping(value = "/addUser", method = {RequestMethod.POST, RequestMethod.GET})
    public String addUser(HttpSession session, Model model,
                          @RequestParam(name = "name", required = false, defaultValue = "") String name,
                          @RequestParam(name = "pass", required = false, defaultValue = "") String password,
                          @RequestParam(name = "path", required = false, defaultValue = "") String path) {
        System.err.println("INFO: addUser called with: " + name);

        if ("".equals(name)) {
            model.addAttribute("added", false);
            model.addAttribute("triedToAdd", false);
        } else {
            model.addAttribute("triedToAdd", true);
            boolean added = Backend.createBackend(name, password, path);
            model.addAttribute("added", added);
            model.addAttribute("name", name);
        }
        return ("addUser");

    }
}
