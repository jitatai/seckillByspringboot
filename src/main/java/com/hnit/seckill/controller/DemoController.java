package com.hnit.seckill.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class DemoController {
    @RequestMapping("hello")
    public String thymeleaf(Model model){
        model.addAttribute("name","jiatai");
        return "hello";
    }
}
