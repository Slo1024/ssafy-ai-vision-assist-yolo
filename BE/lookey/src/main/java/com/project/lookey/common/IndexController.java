package com.project.lookey.common;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class IndexController {

    @GetMapping("/")
    @ResponseBody
    public String index() {
        return "<script>window.location.href='/swagger-ui/index.html';</script>";
    }
    
    @GetMapping("/docs")
    @ResponseBody  
    public String docs() {
        return "<script>window.location.href='/swagger-ui/index.html';</script>";
    }
}