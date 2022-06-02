package cz.cuni.mff.soukups3.PipeWeb.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

@RequestMapping("/")
@Controller
public class IndexController {
    @GetMapping("/")
    public RedirectView redirectView(RedirectAttributes attributes){
        attributes.addAttribute("name", "/Users/Public");
        return new RedirectView("/list");
    }
}
