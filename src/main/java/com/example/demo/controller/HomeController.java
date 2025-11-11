package com.example.demo.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Slf4j
public class HomeController {

    // 기본 경로(localhost:8090) 요청을 하면 index로 이동하도록 기본 세팅
    @GetMapping("/")
    public String home(){
        log.info("GET /...");
        return "index"; // resources/templates
    }

}
