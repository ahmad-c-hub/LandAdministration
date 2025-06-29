package com.example.landadministration.controllers;

import com.example.landadministration.dtos.UsersDTO;
import com.example.landadministration.entities.Users;
import com.example.landadministration.repos.UserRepo;
import com.example.landadministration.services.JWTService;
import com.example.landadministration.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JWTService jwtService;
    @Autowired
    private UserRepo userRepo;

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
    @GetMapping("/get-users")
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

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/update/{id}")
    public UsersDTO updateUserById(@PathVariable Integer id, @RequestBody Users user){
        return userService.updateUser(id,user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/delete/{id}")
    public UsersDTO deleteUserById(@PathVariable Integer id){
        return userService.delete(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/get-user/{id}")
    public UsersDTO getUserById(@PathVariable Integer id){
        return userService.getUserById(id);
    }



}
