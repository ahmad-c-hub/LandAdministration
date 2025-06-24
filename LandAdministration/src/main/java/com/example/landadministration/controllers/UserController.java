package com.example.landadministration.controllers;

import com.example.landadministration.dtos.UsersDTO;
import com.example.landadministration.entities.Users;
import com.example.landadministration.services.JWTService;
import com.example.landadministration.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping()
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JWTService jwtService;

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

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users")
    public List<UsersDTO> getUsers(){
        return userService.getUsers();
    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest request){
        String authHeader = request.getHeader("Authorization");
        System.out.println("Auth Header: "+authHeader);
        if(authHeader != null && authHeader.startsWith("Bearer ")){
            String token = authHeader.substring(7);
            jwtService.revokeToken(token);
            return "Logged out successfully! ";
        }
        return "No token found!";
    }
    @DeleteMapping("/delete/{id}")
    public void deleteUserById(@PathVariable Integer id){
        userService.delete(id);
    }



}
