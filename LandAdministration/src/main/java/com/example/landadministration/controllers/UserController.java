package com.example.landadministration.controllers;

import com.example.landadministration.entities.Users;
import com.example.landadministration.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping()
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public String login(@RequestBody Users user){
        System.out.println(user);
        return userService.verify(user);
    }

    @PostMapping("/register")
    public Users register(@RequestBody Users user){
        System.out.println(user);
        return userService.register(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/set-role/{id}/{role}")
    public void setRole(@PathVariable Integer id, @PathVariable String role){
        userService.setRole(id,role);
    }

}
