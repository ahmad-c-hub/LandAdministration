package com.example.landadministration.controllers;


import com.example.landadministration.dtos.LandDTO;
import com.example.landadministration.dtos.LandOwnerDTO;
import com.example.landadministration.entities.LandOwner;
import com.example.landadministration.services.LandOwnerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(
        origins = "http://localhost:3000",
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS}
)
@RestController
@RequestMapping("/land-owner")
public class LandOwnerController {

    @Autowired
    private LandOwnerService landOwnerService;

    @GetMapping("/owners")
    public Page<LandOwnerDTO> getLandOwners(@RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "10") int size,
                                            @RequestParam(defaultValue = "id") String sortedBy){
        return landOwnerService.getLandOwners(page,size,sortedBy);
    }

    @PostMapping("/add")
    public LandOwnerDTO addLandOwner(@RequestBody LandOwner landOwner){
        return landOwnerService.addLandOwner(landOwner);
    }

    @PostMapping("/{land_id}/assign-owner/{owner_id}")
    public LandDTO assignLandToOwner(@PathVariable Integer land_id, @PathVariable Integer owner_id){
        return landOwnerService.assignLandToOwner(owner_id,land_id);
    }

    @GetMapping("/lands/{id}")
    public Page<LandDTO> getLandsByOwnerId(@PathVariable Integer id,
                                      @RequestParam(defaultValue = "id") String sortedBy,
                                      @RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "10") int size){
        return landOwnerService.getLandsByOwnerId(id,sortedBy,page,size);
    }

    @PutMapping("/update-owner")
    public LandOwnerDTO updateOwnerById(@RequestBody LandOwner landOwner,
                                    @RequestParam Integer id){
        return landOwnerService.updateOwnerById(landOwner,id);
    }

    @DeleteMapping("/delete-owner")
    public String deleteOwnerById(@RequestParam Integer id){
        return landOwnerService.deleteOwnerById(id);
    }
    @GetMapping("/{id}")
    public LandOwnerDTO getLandOwnerById(@PathVariable Integer id){
        return landOwnerService.getLandOwnerById(id);
    }


}
