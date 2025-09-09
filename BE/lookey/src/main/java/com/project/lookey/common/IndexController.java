package com.project.lookey.common;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    @GetMapping("/")
    public String index() {
        return "redirect:https://j13e101.p.ssafy.io/dev/swagger-ui/index.html?url=https://j13e101.p.ssafy.io/dev/v3/api-docs";
    }
    
    @GetMapping("/docs")
    public String docs() {
        return "redirect:https://j13e101.p.ssafy.io/dev/swagger-ui/index.html?url=https://j13e101.p.ssafy.io/dev/v3/api-docs";
    }
}