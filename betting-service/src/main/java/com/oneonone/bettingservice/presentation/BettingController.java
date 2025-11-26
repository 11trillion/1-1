package com.oneonone.bettingservice.presentation;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/bets")
public class BettingController {

    @GetMapping("/test")
    public String test(){
        System.out.println("test");
        return "test";
    }

}
