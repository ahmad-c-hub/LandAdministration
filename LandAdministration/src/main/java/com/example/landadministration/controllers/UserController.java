package com.example.landadministration.controllers;

import com.example.landadministration.dtos.UsersDTO;
import com.example.landadministration.entities.UserLog;
import com.example.landadministration.entities.Users;
import com.example.landadministration.repos.UserLogRepo;
import com.example.landadministration.repos.UserRepo;
import com.example.landadministration.services.JWTService;
import com.example.landadministration.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(
        origins = {
                "http://localhost:3000",           // for local development
                "https://ahmad-c-hub.github.io"    // for production (GitHub Pages)
        },
        allowedHeaders = "*",
        methods = {
                RequestMethod.GET,
                RequestMethod.POST,
                RequestMethod.PUT,
                RequestMethod.DELETE,
                RequestMethod.OPTIONS
        }
)

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

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

    @PreAuthorize("!hasRole('ROLE_USER')")
    @PutMapping("/set-role/{id}/{role}")
    public String setRole(
            @PathVariable Integer id,
            @PathVariable String role,
            @RequestParam(required = false) String reason) {

        Users userNavigating = (Users) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // If the user is the super admin, allow direct role change
        if (userNavigating.getUsername().equalsIgnoreCase("ADMIN")) {
            return userService.setRole(id, role, userNavigating);
        }

        // If reason is not provided, reject the request
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Reason must be provided when requesting role change.");
        }

        // Else, initiate role request workflow
        return userService.requestRoleChange(id, role, reason, userNavigating);
    }


    @PreAuthorize("!hasRole('ROLE_USER')")
    @GetMapping("/get-users")
    public Page<UsersDTO> getUsers(@RequestParam int page,
                                   @RequestParam int size){
        return userService.getUsers(page,size);
    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest request){
        Users userNavigating = (Users) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userService.logout(request, userNavigating);
    }


    @PreAuthorize("!hasRole('ROLE_USER')")
    @DeleteMapping("/delete/{id}")
    public UsersDTO deleteUserById(@PathVariable Integer id){
        Users userNavigating = (Users) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userService.delete(id, userNavigating);
    }

    @PreAuthorize("!hasRole('ROLE_USER')")
    @GetMapping("/get-user/{id}")
    public UsersDTO getUserById(@PathVariable Integer id){
        return userService.getUserById(id);
    }

    @GetMapping("/get-role")
    public String getRole(){
        Users userNavigating = (Users) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userNavigating.getRole().getAuthority();
    }

    @PutMapping("/update-current-user")
    public UsersDTO updateCurrentUser(@RequestBody Users user){
        Users userNavigating = (Users) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userService.updateCurrentUser(userNavigating, user);
    }

    @PutMapping("/change-password")
    public String updatePassword(@RequestBody Map<String, String> passwords) {
        String oldPassword = passwords.get("oldPassword");
        String newPassword = passwords.get("newPassword");

        Users userNavigating = (Users) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        userService.updatePassword(userNavigating, oldPassword, newPassword);
        return "Password updated successfully.";
    }

    @PutMapping("/set-country")
    public String setCountry(@RequestParam String country){
        Users userNavigating = (Users) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!country.isEmpty()){
            userNavigating.setCountry(country);
            userRepo.save(userNavigating);
            return "Country updated to " + country + " successfully.";
        }
        userNavigating.setCountry("");
        userRepo.save(userNavigating);
        return "Country updated to all countries successfully.";

    }
    @GetMapping("/get-country")
    public String getCountry(){
        Users userNavigating = (Users) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(userNavigating.getCountry().isEmpty()){
            return "";
        }
        return userNavigating.getCountry();
    }
    @GetMapping("/current-user")
    public UsersDTO getCurrentUser() {
        Users user = (Users) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UsersDTO userDTO = new UsersDTO(user.getUsername(), user.getRole().getAuthority(),user.isGoogleUser(),user.getCountry(),user.getId());
        return userDTO;
    }




}
