package com.example.landadministration.controllers;

import com.example.landadministration.dtos.UserLogDTO;
import com.example.landadministration.entities.Users;
import com.example.landadministration.repos.UserLogRepo;
import com.example.landadministration.services.UserLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

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
@RequestMapping("/user-log")
public class UserLogController {

    @Autowired
    private UserLogService userLogService;

    @PreAuthorize("!hasRole('ROLE_USER')")
    @GetMapping("/records")
    public Page<UserLogDTO> getUserLogRecords(@RequestParam int page,
                                              @RequestParam int size){
        return userLogService.getUserLogRecords(page,size);
    }

    @PreAuthorize("!hasRole('ROLE_USER')")
    @GetMapping("/records/{id}")
    public Page<UserLogDTO> getUserLogRecordsByUserId(@RequestParam int page,
                                                      @RequestParam int size,
                                                      @PathVariable Integer id){
        return userLogService.getUserLogRecordsByUserId(page, size, id);
    }

    @GetMapping("/current-user")
    public Page<UserLogDTO> getCurrentUserLogRecords(@RequestParam int page,
                                                      @RequestParam int size){
        Users userNavigating = (Users) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userLogService.getCurrentUserLogRecords(page,size, userNavigating);
    }
}
