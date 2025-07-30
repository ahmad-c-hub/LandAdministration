package com.example.landadministration.services;

import com.example.landadministration.dtos.LandDTO;
import com.example.landadministration.dtos.LandOwnerDTO;
import com.example.landadministration.entities.*;
import com.example.landadministration.repos.LandOwnerRepo;
import com.example.landadministration.repos.LandRepo;
import com.example.landadministration.repos.OwnershipHistoryRepo;
import com.example.landadministration.repos.UserLogRepo;
import com.example.landadministration.specifications.LandSpecification;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PagedModel;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class LandService {

    @Value("${opencage.api.key}")
    private String apiKey;

    @Autowired
    private LandRepo landRepo;

    @Autowired
    private LandOwnerRepo landOwnerRepo;

    @Autowired
    private OwnershipHistoryRepo ownershipHistoryRepo;

    @Autowired
    private UserLogRepo userLogRepo;




    public List<LandDTO> getLandRecords(String sortedBy) {
        Users userNavigating = (Users) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<Land> lands;
        if(sortedBy.equals("location")){
            lands = landRepo.findAll(Sort.by(Sort.Direction.ASC,"location"));
        }else if(sortedBy.equals("surfaceAreaDesc")){
            lands = landRepo.findAll(Sort.by(Sort.Direction.DESC,"surfaceArea"));
        }else if(sortedBy.equals("surfaceAreaAsc")){
            lands = landRepo.findAll(Sort.by(Sort.Direction.ASC,"surfaceArea"));
        }else{
            lands = landRepo.findAll(Sort.by(Sort.Direction.ASC,"id"));
        }
        if(userNavigating.getCountry().isEmpty()){
            return getDTOList(lands);
        }else {
            List<Land> landsToReturn = new ArrayList<>();
            for (Land land : lands) {
                if (land.getCountryFromLocation(land.getLocation()).equals(userNavigating.getCountry())) {
                    landsToReturn.add(land);
                }
            }
            return getDTOList(landsToReturn);
        }
    }

    public LandDTO addRecord(Land land) {
        Users userNavigating = (Users) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (Double.isNaN(land.getLongitude())) {
            throw new IllegalStateException("Longitude not entered");
        }
        if (Double.isNaN(land.getLatitude())) {
            throw new IllegalStateException("Latitude not entered");
        }
        if(!userNavigating.getCountry().isEmpty()) {
            if (!Objects.equals(land.getCountryFromLocation(getLocationFromCoordinates(land.getLatitude(), land.getLongitude())), userNavigating.getCountry())) {
                throw new IllegalStateException("User does not have access to the given coordinates");
            }
        }
        if(landRepo.findByLocationCoordinates(land.getLatitude(),land.getLongitude()).isPresent()){
            throw new IllegalStateException("Land already exists at the given coordinates");
        }
        if (land.getSurfaceArea() == 0) {
            land.setSurfaceArea(100);
        }
        if (land.getUsage_type() == null) {
            land.setUsage_type("Residential");
        }
        land.setLocation(getLocationFromCoordinates(land.getLatitude(), land.getLongitude()));
        UserLog userLog = new UserLog();
        userLog.setUser(userNavigating);
        userLog.setAction("ADD_LAND");
        userLog.setTimestamp(LocalDateTime.now());
        Land savedLand = landRepo.save(land);
        userLog.setDescription("User {" + userNavigating.getUsername() + "} added land with id {" + savedLand.getId() + "} " +
                "and with location {" + savedLand.getLocation() + "}.");
        userLogRepo.save(userLog);
        return getDTO(savedLand);
    }

    public String getLocationFromCoordinates(double latitude, double longitude) {
        try {
            String url = String.format(
                    "https://api.opencagedata.com/geocode/v1/json?q=%f,%f&key=%s",
                    latitude, longitude, apiKey
            );

            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.getForObject(url, String.class);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);

            JsonNode components = root
                    .path("results").get(0)
                    .path("components");

            String city = components.path("village").asText(null);
            if (city == null) city = components.path("town").asText(null);
            if (city == null) city = components.path("city").asText("Unknown");

            String state = components.path("state").asText("");
            String country = components.path("country").asText("");

            return String.join(", ", city, state, country).replaceAll("(^,\\s*)|(,\\s*$)", "");

        } catch (Exception e) {
            System.err.println("Reverse geocoding failed: " + e.getMessage());
            return "Unknown";
        }
    }


    public LandDTO updateUsageType(Integer id, String usageType) {
        Optional<Land> landOptional = landRepo.findById(id);
        if(!landOptional.isPresent()){
            throw new IllegalStateException("Land not found");
        }
        Land landToUpdate = landOptional.get();
        if(landToUpdate.getUsage_type() != null){
            Users userNavigating = (Users) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            UserLog userLog = new UserLog();
            userLog.setUser(userNavigating);
            userLog.setAction("UPDATE_USAGE_TYPE");
            userLog.setTimestamp(LocalDateTime.now());
            userLog.setDescription("User {" + userNavigating.getUsername() + "} updated usage type of land with id {" +
                    id + "} from {" + landToUpdate.getUsage_type() +"} to {" + usageType + "}.");
            landToUpdate.setUsage_type(usageType);
            Land updatedLand = landRepo.save(landToUpdate);
            userLogRepo.save(userLog);
            return getDTO(updatedLand);
        }
        throw new IllegalStateException("Usage type not entered");
    }

    public LandDTO getLandRecordById(Integer id) {
        Users userNavigating = (Users) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<Land> landOptional = landRepo.findById(id);
        if(!landOptional.isPresent()){
            throw new IllegalStateException("Land not found");
        }
        Land land = landOptional.get();
        if(!userNavigating.getCountry().isEmpty()) {
            if (!land.getCountryFromLocation(land.getLocation()).equals(userNavigating.getCountry())) {
                throw new IllegalStateException("Land not found in "+userNavigating.getCountry());
            }
        }
        return getDTO(land);
    }

    public LandDTO deleteLandRecordById(Integer id) {
        Users userNavigating = (Users) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<Land> landOptional = landRepo.findById(id);
        if(!landOptional.isPresent()){
            throw new IllegalStateException("Land not found");
        }
        if(landOptional.get().getLandOwner()!=null){
            throw new IllegalStateException("Land owner is assigned to this land, cannot delete the land. Please unassign the land owner first and then delete the land.");
        }
        if(!userNavigating.getCountry().isEmpty()) {
            if (!landOptional.get().getCountryFromLocation(landOptional.get().getLocation()).equals(userNavigating.getCountry())) {
                throw new IllegalStateException("Land not found in "+userNavigating.getCountry());
            }
        }
        UserLog userLog = new UserLog();
        userLog.setUser(userNavigating);
        userLog.setAction("DELETE_LAND");
        userLog.setTimestamp(LocalDateTime.now());
        userLog.setDescription("User {" + userNavigating.getUsername() + "} deleted land with id {" + id + "}.");
        landRepo.deleteById(id);
        userLogRepo.save(userLog);
        Land landToDelete = landOptional.get();
        return getDTO(landToDelete);
    }

    public List<LandDTO> getLandRecordsByUsageType(String usageType,String sortedBy) {
        Users userNavigating = (Users) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<Land> lands;
        if(sortedBy.equals("location")){
            lands = landRepo.findByUsageType(usageType,Sort.by(Sort.Direction.ASC,"location"));
        }else if(sortedBy.equals("surfaceAreaDesc")){
            lands = landRepo.findByUsageType(usageType,Sort.by(Sort.Direction.DESC,"surfaceArea"));
        }else if(sortedBy.equals("surfaceAreaAsc")){
            lands = landRepo.findByUsageType(usageType,Sort.by(Sort.Direction.ASC,"surfaceArea"));
        }else{
            lands = landRepo.findByUsageType(usageType, Sort.by (Sort.Direction.ASC, "id"));
        }
        if(userNavigating.getCountry().isEmpty()) {
            return getDTOList(lands);
        }else{
            List<Land> landsToReturn = new ArrayList<>();
            for(Land land : lands){
                if(land.getCountryFromLocation(land.getLocation()).equals(userNavigating.getCountry())){
                    landsToReturn.add(land);
                }
            }
            return getDTOList(landsToReturn);

        }

    }

    public List<LandDTO> filterBySurfaceArea(double min, double max, String sortedBy) {
        if(min > max){
            throw new IllegalStateException("Min value is greater than max value");
        }
        List<Land> lands;
        if(sortedBy.equals("location")){
            lands = landRepo.filterBySurfaceArea(min,max,Sort.by(Sort.Direction.ASC,"location"));
        }else if(sortedBy.equals("surfaceAreaDesc")){
            lands = landRepo.filterBySurfaceArea(min,max,Sort.by(Sort.Direction.DESC,"surfaceArea"));
        }else if(sortedBy.equals("surfaceAreaAsc")){
            lands = landRepo.filterBySurfaceArea(min,max,Sort.by(Sort.Direction.ASC,"surfaceArea"));
        }else{
            lands = landRepo.filterBySurfaceArea(min,max,Sort.by(Sort.Direction.ASC,"id"));
        }
        if(lands.size() == 0){
            throw new IllegalStateException("No land found with the given surface area");
        }
        return getDTOList(lands);
    }

    public List<LandDTO> getLandRecordsByLocation(String location, String sortedBy) {
        List<Land> lands;
        if(sortedBy.equals("surfaceAreaDesc")) {
            lands = landRepo.findByLocation(location, Sort.by(Sort.Direction.DESC, "surfaceArea"));
        }else if(sortedBy.equals("surfaceAreaAsc")) {
            lands = landRepo.findByLocation(location, Sort.by(Sort.Direction.ASC, "surfaceArea"));
        }else{
            lands = landRepo.findByLocation(location, Sort.by(Sort.Direction.ASC, "id"));
        }
        return getDTOList(lands);
    }

    public Page<LandDTO> getPagedLandRecords(String sortedBy, int page, int size) {
        Sort sort;

        if (sortedBy.equals("location")) {
            sort = Sort.by(Sort.Direction.ASC, "location");
        } else if (sortedBy.equals("surfaceAreaDesc")) {
            sort = Sort.by(Sort.Direction.DESC, "surfaceArea");
        } else if (sortedBy.equals("surfaceAreaAsc")) {
            sort = Sort.by(Sort.Direction.ASC, "surfaceArea");
        } else {
            sort = Sort.by(Sort.Direction.ASC, "id");
        }

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Land> landPage = landRepo.findAllLandsPaged(pageable);

        if (landPage.isEmpty()) {
            throw new IllegalStateException("No land found");
        }

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


    public Page<LandDTO> getPagedLandsByLocation(String location, String sortedBy, int page, int size) {
        Sort sort;
        if (sortedBy.equals("location")) {
            sort = Sort.by(Sort.Direction.ASC, "location");
        } else if (sortedBy.equals("surfaceAreaDesc")) {
            sort = Sort.by(Sort.Direction.DESC, "surfaceArea");
        } else if (sortedBy.equals("surfaceAreaAsc")) {
            sort = Sort.by(Sort.Direction.ASC, "surfaceArea");
        } else {
            sort = Sort.by(Sort.Direction.ASC, "id");
        }

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Land> landPage = landRepo.findByLocationPage(location, pageable);

        if (landPage.isEmpty()) {
            throw new IllegalStateException("No land found with the given location");
        }

        return landPage.map(land -> {
            LandDTO dto = new LandDTO(land.getId(), land.getLocation(), land.getSurfaceArea(), land.getUsage_type(),getDTO(land.getLandOwner()));
            dto.setLocationCoordinates(land.getLatitude(), land.getLongitude());
            return dto;
        });
    }


    public Page<LandDTO> filterBySurfaceAreaPaged(double min, double max, String sortedBy, int page, int size) {
        if (min >= max) {
            throw new IllegalStateException("Min value must be less than max value");
        }

        Sort sort;
        if (sortedBy.equals("location")) {
            sort = Sort.by(Sort.Direction.ASC, "location");
        } else if (sortedBy.equals("surfaceAreaDesc")) {
            sort = Sort.by(Sort.Direction.DESC, "surfaceArea");
        } else if (sortedBy.equals("surfaceAreaAsc")) {
            sort = Sort.by(Sort.Direction.ASC, "surfaceArea");
        } else {
            sort = Sort.by(Sort.Direction.ASC, "id");
        }

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Land> landPage = landRepo.filterBySurfaceAreaPage(min, max, pageable);

        if (landPage.isEmpty()) {
            throw new IllegalStateException("No land found within the given surface area");
        }

        return landPage.map(land -> {
            LandDTO dto = new LandDTO(land.getId(), land.getLocation(), land.getSurfaceArea(), land.getUsage_type(),getDTO(land.getLandOwner()));
            dto.setLocationCoordinates(land.getLatitude(), land.getLongitude());
            return dto;
        });
    }




    public Page<LandDTO> getLandRecordsByUsageTypePaged(String usageType, String sortedBy, int page, int size) {
        Sort sort;
        if (sortedBy.equals("location")) {
            sort = Sort.by(Sort.Direction.ASC, "location");
        } else if (sortedBy.equals("surfaceAreaDesc")) {
            sort = Sort.by(Sort.Direction.DESC, "surfaceArea");
        } else if (sortedBy.equals("surfaceAreaAsc")) {
            sort = Sort.by(Sort.Direction.ASC, "surfaceArea");
        } else {
            sort = Sort.by(Sort.Direction.ASC, "id");
        }

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Land> landPage = landRepo.findByUsageTypePaged(usageType, pageable);

        if (landPage.isEmpty()) {
            throw new IllegalStateException("No land found with the given usage type");
        }

        return landPage.map(land -> {
            LandDTO dto = new LandDTO(land.getId(), land.getLocation(), land.getSurfaceArea(), land.getUsage_type(),getDTO(land.getLandOwner()));
            dto.setLocationCoordinates(land.getLatitude(), land.getLongitude());
            return dto;
        });
    }

    public List<LandDTO> getDTOList(List<Land> lands){
        List<LandDTO> landDTOList = new ArrayList<>();
        for(Land land : lands){
            LandDTO landDTO = new LandDTO(land.getId(),land.getLocation(),land.getSurfaceArea(),land.getUsage_type(),getDTO(land.getLandOwner()));
            landDTO.setLocationCoordinates(land.getLatitude(),land.getLongitude());
            landDTOList.add(landDTO);
        }
        return landDTOList;
    }


    public LandDTO getDTO(Land land){
        LandDTO landDTO = new LandDTO(land.getId(),land.getLocation(),land.getSurfaceArea(),land.getUsage_type(),getDTO(land.getLandOwner()));
        landDTO.setLocationCoordinates(land.getLatitude(),land.getLongitude());
        return landDTO;
    }
    public Page<LandDTO> searchLands(String location, String usageType, String ownerName, String sortedBy, int page, int size) {
        Specification<Land> spec = (root, query, cb) -> cb.conjunction();

        if (location != null && !location.isEmpty()) {
            spec = spec.and(LandSpecification.hasLocation(location));
        }
        if (usageType != null && !usageType.isEmpty()) {
            spec = spec.and(LandSpecification.hasUsageType(usageType));
        }
        if (ownerName != null && !ownerName.isEmpty()) {
            spec = spec.and(LandSpecification.hasOwnerName(ownerName));
        }

        Sort sort = Sort.by(Sort.Direction.ASC, sortedBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Land> landPage = landRepo.findAll(spec, pageable);

        return landPage.map(land -> {
            LandDTO dto = new LandDTO(land.getId(), land.getLocation(), land.getSurfaceArea(), land.getUsage_type(),getDTO(land.getLandOwner()));
            dto.setLocationCoordinates(land.getLatitude(), land.getLongitude());
            return dto;
        });
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

    public LandDTO unassignOwner(Integer landId) {
        Users user = (Users) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Land land = landRepo.findById(landId)
                .orElseThrow(() -> new IllegalStateException("Land not found"));
        if(land.getLandOwner()==null){
            throw new IllegalStateException("There is no land owner assigned to this land");
        }
        LandOwner owner = land.getLandOwner();

        Optional<OwnershipHistory> historyOptional = ownershipHistoryRepo.findActiveByOwner_IdAndLand_Id(owner.getId(),landId);
        historyOptional.get().setOwnershipEnd(LocalDateTime.now());
        land.setLandOwner(null);
        Land updatedLand = landRepo.save(land);
        UserLog userLog = new UserLog();
        userLog.setUser(user);
        userLog.setAction("UNASSIGN_OWNER");
        userLog.setTimestamp(LocalDateTime.now());
        userLog.setDescription("User {" + user.getUsername() + "} unassigned land with id {" + landId + "} from owner {" + owner.getId() + "}.");
        userLogRepo.save(userLog);
        return getDTO(updatedLand);
    }

    public Integer getUnassignedLands() {
        Users userNavigating = (Users) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!userNavigating.getCountry().isEmpty()) {
            List<Land> unassignedLandsByCountry = new ArrayList<>();
            for (Land land : landRepo.getUnassignedLands()) {
                if (land.getCountryFromLocation(land.getLocation()).equals(userNavigating.getCountry())) {
                    unassignedLandsByCountry.add(land);
                }
            }
            return unassignedLandsByCountry.size();
        }
        System.out.println("Unassigned lands: " + landRepo.getUnassignedLands().size());
        return landRepo.getUnassignedLands().size();
    }
}
