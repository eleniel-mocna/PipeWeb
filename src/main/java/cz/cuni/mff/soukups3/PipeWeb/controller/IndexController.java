package cz.cuni.mff.soukups3.PipeWeb.controller;

import cz.cuni.mff.soukups3.PipeWeb.Backend;
import cz.cuni.mff.soukups3.PipeWeb.DefaultFolderTree;
import cz.cuni.mff.soukups3.PipeWeb.FolderTree;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

@Controller
public class IndexController {
//    @GetMapping("/")
//    public RedirectView redirectView(RedirectAttributes attributes, HttpSession session){
//        if (session.getAttribute("user")==null) {
//            return new RedirectView("/login");
//        }
//        else {
//            return new RedirectView("/index");
//        }
//    }

}
