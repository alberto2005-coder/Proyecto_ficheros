package com.example.demo.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserController {

    @GetMapping("/pruebas")
    public String pruebas(Model model) {
        model.addAttribute("title", "Texto");
        model.addAttribute("nombre", "Alberto");
        model.addAttribute("apellido", "Ortiz");
        return "pruebas";
    }    
}
