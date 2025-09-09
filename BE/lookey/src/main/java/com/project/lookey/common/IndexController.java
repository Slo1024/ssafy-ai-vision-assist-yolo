package com.project.lookey.common;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    @GetMapping("/")
    public String index() {
        return "redirect:/swagger-ui/index.html?url=/v3/api-docs";
    }
    
    @GetMapping("/docs")
    public String docs() {
        return "redirect:/swagger-ui/index.html?url=/v3/api-docs";
    }
}