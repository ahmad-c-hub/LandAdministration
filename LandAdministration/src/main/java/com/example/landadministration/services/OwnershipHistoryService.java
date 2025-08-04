package com.example.landadministration.services;


import com.example.landadministration.dtos.LandDTO;
import com.example.landadministration.dtos.LandOwnerDTO;
import com.example.landadministration.dtos.OwnershipHistoryDTO;
import com.example.landadministration.entities.*;
import com.example.landadministration.repos.LandOwnerRepo;
import com.example.landadministration.repos.LandRepo;
import com.example.landadministration.repos.OwnershipHistoryRepo;
import com.example.landadministration.repos.UserLogRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Optional;

@Service
public class OwnershipHistoryService {

    @Autowired
    private OwnershipHistoryRepo ownershipHistoryRepo;

    @Autowired
    private LandOwnerRepo landOwnerRepo;

    @Autowired
    private LandRepo landRepo;

    @Autowired
    private UserLogRepo userLogRepo;


    public Page<OwnershipHistoryDTO> getLandHistoryById(int page, int size, Integer id) {
        Users userNavigating = (Users) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<Land> land;
        if(userNavigating.getCountry().isEmpty()){
            land = landRepo.findById(id);
            if (land.isEmpty()) {
                throw new IllegalStateException("Land not found");
            }
        }else{
            land = landRepo.findByIdAndCountry(id, userNavigating.getCountry());
            if (land.isEmpty()) {
                throw new IllegalStateException("Land not found in "+userNavigating.getCountry()+".");
            }
        }
        Pageable pageable = PageRequest.of(page,size);
        Page<OwnershipHistory> landHistory = ownershipHistoryRepo.findByLand_Id(id,pageable);
        return landHistory.map(ownershipHistory -> {
            OwnershipHistoryDTO dto = new OwnershipHistoryDTO(getLandDTO(ownershipHistory.getLand()),
                    getOwnerDTO(ownershipHistory.getOwner()),
                    ownershipHistory.getOwnershipStart(),
                    ownershipHistory.getOwnershipEnd(),
                    ownershipHistory.getCreatedAt());
            return dto;
        });
    }

    public Page<OwnershipHistoryDTO> getAllRecords(int page, int size){
        Users userNavigating = (Users) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Pageable pageable = PageRequest.of(page,size);
        Page<OwnershipHistory> historyPaged;
        if(userNavigating.getCountry().isEmpty()){
            historyPaged = ownershipHistoryRepo.findAll(pageable);
            if(historyPaged.isEmpty()){
                throw new IllegalStateException("No records found");
            }
        }else{
            historyPaged = ownershipHistoryRepo.findAllByCountry(userNavigating.getCountry(),pageable);
            if(historyPaged.isEmpty()){
                throw new IllegalStateException("No records found in "+userNavigating.getCountry()+".");
            }
        }
        return historyPaged.map(ownershipHistory ->{
            OwnershipHistoryDTO dto = new OwnershipHistoryDTO(getLandDTO(ownershipHistory.getLand()),
                    getOwnerDTO(ownershipHistory.getOwner()),
                    ownershipHistory.getOwnershipStart(),
                    ownershipHistory.getOwnershipEnd(),
                    ownershipHistory.getCreatedAt());
            return dto;
        });
    }

    public Page<OwnershipHistoryDTO> getOwnershipHistoryByOwnerId(int page, int size, Integer ownerId) {
        Users userNavigating = (Users) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<LandOwner> landOwner;
        if(userNavigating.getCountry().isEmpty()) {
            landOwner = landOwnerRepo.findById(ownerId);
            if (!landOwner.isPresent()) {
                throw new IllegalStateException("Land Owner not found");
            }
        }else{
            landOwner= landOwnerRepo.findByIdAndCountry(ownerId, userNavigating.getCountry());
            if (!landOwner.isPresent()) {
                throw new IllegalStateException("Land Owner not found in "+userNavigating.getCountry()+".");
            }
        }

        Pageable pageable = PageRequest.of(page,size);
        Page<OwnershipHistory> ownerHistory = ownershipHistoryRepo.findByOwner_Id(ownerId,pageable);
        return ownerHistory.map(ownershipHistory ->{
            OwnershipHistoryDTO dto = new OwnershipHistoryDTO(getLandDTO(ownershipHistory.getLand()),
                    getOwnerDTO(ownershipHistory.getOwner()),
                    ownershipHistory.getOwnershipStart(),
                    ownershipHistory.getOwnershipEnd(),
                    ownershipHistory.getCreatedAt());
            return dto;
        });
    }

    public List<OwnershipHistory> getOwnershipHistoryAsList(){
        Users userNavigating = (Users) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<OwnershipHistory> historyList;
        if(userNavigating.getCountry().isEmpty()){
            return ownershipHistoryRepo.findAll();
        }
        return ownershipHistoryRepo.findAllByCountryList(userNavigating.getCountry());
    }


    public LandOwnerDTO getOwnerDTO(LandOwner landOwner){
        if(landOwner==null){
            return null;
        }
        Integer landCount = 0;
        if(landOwner.getLands()!=null){
            landCount = landOwner.getLands().size();
        }
        LandOwnerDTO landOwnerDTO =  new LandOwnerDTO(landOwner.getId(),
                landOwner.getFirstName()+" "+landOwner.getLastName(),
                landOwner.getPhoneNb(),
                landOwner.getEmailAddress(),
                landCount,
                Period.between(landOwner.getDateOfBirth(), LocalDate.now()).getYears(),
                landOwner.getCountry());
        return landOwnerDTO;
    }

    public LandDTO getLandDTO(Land land){
        LandDTO landDTO = new LandDTO(land.getId(),land.getLocation(),
                land.getSurfaceArea(),
                land.getUsage_type(),
                getOwnerDTO(land.getLandOwner()));
        landDTO.setLocationCoordinates(land.getLatitude(),land.getLongitude());
        return landDTO;
    }

    public String deleteAllRecords() {
        Users userNavigating = (Users) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        ownershipHistoryRepo.deleteAll();
        UserLog userLog = new UserLog();
        userLog.setUser(userNavigating);
        userLog.setAction("DELETE_ALL_HISTORY_RECORDS");
        userLog.setTimestamp(java.time.LocalDateTime.now());
        userLog.setDescription("All records deleted successfully");
        userLogRepo.save(userLog);

        return "All records deleted successfully";
    }

}
