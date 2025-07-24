package com.example.landadministration.controllers;

import com.example.landadministration.dtos.LandDTO;
import com.example.landadministration.entities.Land;
import com.example.landadministration.services.LandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
@RequestMapping("/land")
public class LandController {

    @Autowired
    private LandService landService;

    @PostMapping("/add")
    public LandDTO addLandRecord(@RequestBody Land land){
        return landService.addRecord(land);
    }

    @GetMapping("/records/{sortedBy}")
    public List<LandDTO> getLandRecords(@PathVariable String sortedBy){
        return landService.getLandRecords(sortedBy);
    }

    @PutMapping("/update-usage-type/{id}/{usage_type}")
    public LandDTO updateUsageType(@PathVariable Integer id, @PathVariable String usage_type){
        return landService.updateUsageType(id, usage_type);
    }

    @GetMapping("/get-unassigned-lands")
    public Integer getUnassignedLands(){
        return landService.getUnassignedLands();
    }

    @GetMapping("/get/{id}")
    public LandDTO getLandRecordById(@PathVariable Integer id){
        return landService.getLandRecordById(id);
    }

    @DeleteMapping("/delete/{id}")
    public LandDTO deleteLandRecordById(@PathVariable Integer id){
        return landService.deleteLandRecordById(id);
    }

    @GetMapping("/usage-type/{usage_type}/{sortedBy}")
    public List<LandDTO> getLandRecordsByUsageType(@PathVariable String usage_type, @PathVariable String sortedBy){
        return landService.getLandRecordsByUsageType(usage_type,sortedBy);
    }

    @GetMapping("/surface-area/{min}/{max}/{sortedBy}")
    public List<LandDTO> filterBySurfaceArea(@PathVariable double min, @PathVariable double max, @PathVariable String sortedBy){
        return landService.filterBySurfaceArea(min,max,sortedBy);
    }

    @PostMapping("/unassign-owner/{id}")
    public LandDTO unassignOwner(@PathVariable Integer id){
        return landService.unassignOwner(id);
    }

    @GetMapping("/location/{location}/{sortedBy}")
    public List<LandDTO> getLandRecordsByLocation(@PathVariable String location, @PathVariable String sortedBy){
        return landService.getLandRecordsByLocation(location,sortedBy);
    }

    @GetMapping("/records-paged/{sortedBy}")
    public Page<LandDTO> getPagedLands(@PathVariable String sortedBy, @RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "10") int size) {
        return landService.getPagedLandRecords(sortedBy,page,size);
    }

    @GetMapping("/location-paged/{location}/{sortedBy}")
    public Page<LandDTO> getPagedLandsByLocation(@PathVariable String location, @PathVariable String sortedBy, @RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "10") int size) {
        return landService.getPagedLandsByLocation(location,sortedBy,page,size);
    }

    @GetMapping("/surface-area-paged/{min}/{max}/{sortedBy}")
    public Page<LandDTO> filterBySurfaceAreaPaged(@PathVariable double min,
                                                  @PathVariable double max,
                                                  @PathVariable String sortedBy,
                                                  @RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "10") int size) {
        return landService.filterBySurfaceAreaPaged(min,max,sortedBy,page,size);
    }

    @GetMapping("usage-type-paged/{usage_type}/{sortedBy}")
    public Page<LandDTO> getLandRecordsByUsageTypePaged(@PathVariable String usage_type,
                                                        @PathVariable String sortedBy,
                                                        @RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "10") int size) {
        return landService.getLandRecordsByUsageTypePaged(usage_type,sortedBy,page,size);
    }
    @GetMapping("/search")
    public Page<LandDTO> searchLands(
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String usageType,
            @RequestParam(required = false) String ownerName,
            @RequestParam(defaultValue = "id") String sortedBy,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "0") int page ) {

        return landService.searchLands(location, usageType, ownerName, sortedBy, page, size);
    }
}
