package com.coltsclub.tusa.website.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.ui.Model;


@Controller
class HomeController {

    @GetMapping("/")
    fun home(model: Model): String {
        model.addAttribute("message", "Добро пожаловать на мой сайт!")
        return "home" // указывает на шаблон home.html
    }

}