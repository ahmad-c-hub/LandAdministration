package com.example.landadministration.services;

import com.example.landadministration.dtos.LandDTO;
import com.example.landadministration.dtos.LandOwnerDTO;
import com.example.landadministration.entities.*;
import com.example.landadministration.repos.LandOwnerRepo;
import com.example.landadministration.repos.LandRepo;
import com.example.landadministration.repos.OwnershipHistoryRepo;
import com.example.landadministration.repos.UserLogRepo;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class LandOwnerService {

    @Autowired
    private LandOwnerRepo landOwnerRepo;

    @Autowired
    private LandRepo landRepo;

    @Autowired
    private OwnershipHistoryRepo ownershipHistoryRepo;

    @Autowired
    private UserLogRepo userLogRepo;

    public Page<LandOwnerDTO> getLandOwners(int page, int size, String sortedBy) {
        Sort sort;
        if(sortedBy.equals("id")){
            sort = Sort.by(Sort.Direction.ASC,"id");
        }else if(sortedBy.equals("firstName")){
            sort = Sort.by(Sort.Direction.ASC,"firstName");
        }else if(sortedBy.equals("lastName")){
            sort = Sort.by(Sort.Direction.ASC,"lastName");
        }else{
            sort = Sort.by(Sort.Direction.ASC,"emailAddress");
        }
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<LandOwner> landPage = landOwnerRepo.findAll(pageable);
        return landPage.map(landOwner -> {
            Integer landCount = 0;
            if(landOwner.getLands()!=null){
                landCount = landOwner.getLands().size();
            }
            LandOwnerDTO dto = new LandOwnerDTO(
                    landOwner.getId(),
                    landOwner.getFirstName() + " " + landOwner.getLastName(),
                    landOwner.getPhoneNb(),
                    landOwner.getEmailAddress(),
                    landCount,
                    Period.between(landOwner.getDateOfBirth(), LocalDate.now()).getYears(),
                    landOwner.getCountry()
            );
            return dto;
        });
    }

    public LandOwnerDTO addLandOwner(LandOwner landOwner) {
        Users userNavigating = (Users) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(landOwner.getFirstName()==null){
            throw new IllegalStateException("First name not entered");
        }
        if(landOwner.getLastName()==null){
            throw new IllegalStateException("Last name not entered");
        }
        if(landOwner.getPhoneNb()==null){
            throw new IllegalStateException("Phone number not entered");
        }
        if(landOwner.getEmailAddress()==null){
            throw new IllegalStateException("Email address not entered");
        }
        if(landOwner.getDateOfBirth()==null){
            throw new IllegalStateException("Date of birth not entered");
        }
        if(landOwner.getDateOfBirth().isAfter(LocalDate.now())){
            throw new IllegalStateException("Date of birth cannot be in the future");
        }
        Optional<LandOwner> landOwnerOptional = landOwnerRepo.findByFirstNameAndLastName(landOwner.getFirstName(), landOwner.getLastName());
        if(landOwnerOptional.isPresent()){
            throw new IllegalStateException("Land owner already exists with the given first name and last name");
        }
        Optional<LandOwner> landOwnerOptional1 = landOwnerRepo.findByEmailAddress(landOwner.getEmailAddress());
        if(landOwnerOptional1.isPresent()){
            throw new IllegalStateException("Land owner already exists with the given email address");
        }
        LandOwner savedLandOwner = landOwnerRepo.save(landOwner);
        UserLog userLog = new UserLog();
        userLog.setUser(userNavigating);
        userLog.setAction("ADD_LAND_OWNER");
        userLog.setTimestamp(LocalDateTime.now());
        userLog.setDescription("User {"+ userNavigating.getUsername()+"} added owner with name {"+savedLandOwner.getFirstName()+" "+
                savedLandOwner.getLastName()+"} and id {"+savedLandOwner.getId()+"}.");
        userLogRepo.save(userLog);

        return getDTO(savedLandOwner);
    }

    //When we reassign the same land to the same owner in which the land was already assigned to this owner but was unassigned,
    // it just updates the old history record and does not create a new record
    public LandDTO assignLandToOwner(Integer ownerId, Integer landId) {
        Users userNavigating = (Users) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        LandOwner owner = landOwnerRepo.findById(ownerId)
                .orElseThrow(() -> new IllegalStateException("Land owner not found"));

        Land land = landRepo.findById(landId)
                .orElseThrow(() -> new IllegalStateException("Land not found"));

        OwnershipHistoryId id = new OwnershipHistoryId(ownershipHistoryRepo.getNextRecordId(),land.getId(), owner.getId());

        Optional<OwnershipHistory> historyOptional = ownershipHistoryRepo.findById(id);
        if(historyOptional.isPresent()&&historyOptional.get().getOwnershipEnd()==null){
            throw new IllegalStateException("Land already assigned to the land owner");
        }

        Integer oldId = -1;
        Optional<OwnershipHistory> historyOpt = ownershipHistoryRepo.findActiveByLandId(landId);
        if(historyOpt.isPresent()){
            OwnershipHistory history = historyOpt.get();
            history.setOwnershipEnd(LocalDateTime.now());
            oldId = history.getOwner().getId();
            ownershipHistoryRepo.save(history);
        }

        land.setLandOwner(owner);
        Land landSaved = landRepo.save(land);
        UserLog userLog = new UserLog();
        userLog.setUser(userNavigating);
        userLog.setAction("TRANSFER_OWNERSHIP");
        userLog.setTimestamp(LocalDateTime.now());
        String old = oldId == -1 ? "N/A" : oldId.toString();
        userLog.setDescription("User {" + userNavigating.getUsername() + "} transferred ownership of land with id" +
                " {" + landId + "} from land owner with id {" + old + "} to land owner with id {" + ownerId + "}.");
        userLogRepo.save(userLog);
        LandDTO landDTO = new LandDTO(
                land.getId(),
                land.getLocation(),
                land.getSurfaceArea(),
                land.getUsage_type(),
                getDTO(land.getLandOwner())
        );
        landDTO.setLocationCoordinates(land.getLatitude(), land.getLongitude());

        OwnershipHistory historyRecord = new OwnershipHistory();

        historyRecord.setId(id);

        historyRecord.setLand(landSaved);

        historyRecord.setOwner(owner);

        historyRecord.setOwnershipStart(LocalDateTime.now());

        historyRecord.setCreatedAt(LocalDateTime.now());

        ownershipHistoryRepo.save(historyRecord);

        return landDTO;
    }



    public Page<LandDTO> getLandsByOwnerId(Integer id, String sortedBy, int page, int size) {
        Optional<LandOwner> landOwnerOptional = landOwnerRepo.findById(id);
        if(!landOwnerOptional.isPresent()){
            throw new IllegalStateException("Land owner not found");
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, sortedBy));
        Page<Land> landPage = landRepo.findLandsByOwnerId(id, pageable);
        return landPage.map(land -> {
            LandDTO dto = new LandDTO(
                    land.getId(),
                    land.getLocation(),
                    land.getSurfaceArea(),
                    land.getUsage_type(),
                    getDTO(land.getLandOwner())
            );
            dto.setLocationCoordinates(land.getLatitude(), land.getLongitude());
            return dto;
        });
    }

    public LandOwnerDTO updateOwnerById(LandOwner landOwner, Integer id) {
        Users userNavigating = (Users) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<LandOwner> landOwnerOptional = landOwnerRepo.findById(id);
        if(!landOwnerOptional.isPresent()){
            throw new IllegalStateException("Land owner not found");
        }
        LandOwner ownerToUpdate = landOwnerOptional.get();
        if(landOwner.getLastName()!=null){
            ownerToUpdate.setLastName(landOwner.getLastName());
        }
        if(landOwner.getPhoneNb()!=null){
            Optional<LandOwner> landOwnerOptionalPhone = landOwnerRepo.findByPhoneNb(landOwner.getPhoneNb());
            if(landOwnerOptionalPhone.isPresent()&&landOwnerOptionalPhone.get().getId()!=id){
                throw new IllegalStateException("Phone number already exists");
            }
            ownerToUpdate.setPhoneNb(landOwner.getPhoneNb());
        }
        if(landOwner.getEmailAddress()!=null){
            Optional<LandOwner> landOwnerOptionalEmail = landOwnerRepo.findByEmailAddress(landOwner.getEmailAddress());
            if(landOwnerOptionalEmail.isPresent()&&landOwnerOptionalEmail.get().getId()!=id){
                throw new IllegalStateException("Email address already exists");
            }
            ownerToUpdate.setEmailAddress(landOwner.getEmailAddress());
        }
        LandOwner updatedOwner = landOwnerRepo.save(ownerToUpdate);
        UserLog userLog = new UserLog();
        userLog.setUser(userNavigating);
        userLog.setAction("UPDATE_LAND_OWNER");
        userLog.setTimestamp(LocalDateTime.now());
        userLog.setDescription("User {"+userNavigating.getUsername() +"} added owner with name {"+updatedOwner.getFirstName()+" "+
                updatedOwner.getLastName()+"} and id {"+updatedOwner.getId()+"}.");

        return getDTO(updatedOwner);
    }

    public String deleteOwnerById(Integer id) {
        Optional<LandOwner> landOwnerOptional = landOwnerRepo.findById(id);
        if(!landOwnerOptional.isPresent()){
            throw new IllegalStateException("Land owner not found");
        }
        LandOwner landOwnerToDelete = landOwnerOptional.get();
        if(landOwnerToDelete.getLands()!=null){
            if(landOwnerToDelete.getLands().size()>0){
                throw new IllegalStateException("Land owner has lands assigned to him and cannot be deleted");
            }
        }
        landOwnerRepo.deleteById(id);
        return "Owner successfully deleted with id: " + id + " and name: " + landOwnerToDelete.getFirstName() + " "
                + landOwnerToDelete.getLastName()  + "\n\n";
    }

    public LandOwnerDTO getLandOwnerById(Integer id) {
        Optional<LandOwner> landOwnerOptional = landOwnerRepo.findById(id);
        if(!landOwnerOptional.isPresent()){
            throw new IllegalStateException("Land owner not found");
        }
        LandOwner landOwner = landOwnerOptional.get();
        return getDTO(landOwner);
    }

    public List<LandDTO> getDTOListLand(List<Land> lands){
        List<LandDTO> landDTOList = new ArrayList<>();
        for(Land land : lands){
            LandDTO landDTO = new LandDTO(
                    land.getId(),
                    land.getLocation(),
                    land.getSurfaceArea(),
                    land.getUsage_type(),
                    getDTO(land.getLandOwner())
            );
            landDTO.setLocationCoordinates(land.getLatitude(), land.getLongitude());
            landDTOList.add(landDTO);
        }
        return landDTOList;
    }

    public List<LandOwnerDTO> getDTOList(List<LandOwner> landOwners){
        List<LandOwnerDTO> landOwnerDTOS = new ArrayList<>();
        for (LandOwner landOwner : landOwners) {
            Integer landCount = 0;
            if(landOwner.getLands()!=null){
                landCount = landOwner.getLands().size();
            }
            LandOwnerDTO landOwnerDTO = getDTO(landOwner);
            landOwnerDTOS.add(landOwnerDTO);
        }
        return landOwnerDTOS;
    }

    public LandOwnerDTO getDTO(LandOwner landOwner){
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
}
