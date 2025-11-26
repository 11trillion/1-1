package com.oneonone.pointservice;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/points")
public class PointController {

    @GetMapping("/test")
    public String test (){
        System.out.println("test");
        return "좋아요";
    }
}
