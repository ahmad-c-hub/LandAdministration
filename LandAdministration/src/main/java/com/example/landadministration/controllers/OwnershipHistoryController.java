package com.example.landadministration.controllers;


import com.example.landadministration.dtos.OwnershipHistoryDTO;
import com.example.landadministration.entities.OwnershipHistory;
import com.example.landadministration.repos.OwnershipHistoryRepo;
import com.example.landadministration.services.OwnershipHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
@RequestMapping("/ownership-history")
public class OwnershipHistoryController {

    @Autowired
    private OwnershipHistoryService ownershipHistoryService;
    @Autowired
    private OwnershipHistoryRepo ownershipHistoryRepository;

    @GetMapping("/records")
    public Integer getOwnershipHistoryRecordsSize(){
        return ownershipHistoryRepository.findAll().size();
    }

    @GetMapping("/recordss")
    public List<OwnershipHistoryDTO> getAllRecords(){
        return ownershipHistoryService.getAllRecords();
    }

    /*@GetMapping("/land")
    public Page<OwnershipHistoryDTO> getLandHistoryById(@RequestParam int size,
                                                        @RequestParam int page,
                                                        @RequestParam Integer id){
        return ownershipHistoryService.getLandHistoryById(page,size,id);
    }*/

    @GetMapping("/owner")
    public Page<OwnershipHistoryDTO> getOwnerHistoryById(@RequestParam int page,
                                                         @RequestParam int size,
                                                         @RequestParam Integer id){
        return ownershipHistoryService.getOwnershipHistoryByOwnerId(page,size,id);
    }
}
