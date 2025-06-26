package com.example.landadministration.services;

import com.example.landadministration.dtos.LandDTO;
import com.example.landadministration.entities.Land;
import com.example.landadministration.repos.LandRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedModel;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class LandService {

    @Autowired
    private LandRepo landRepo;


    public List<LandDTO> getLandRecords(String sortedBy) {
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
        return getDTOList(lands);
    }

    public LandDTO addRecord(Land land) {
        if(land.getLocation()==null){
            throw new IllegalStateException("Location not entered");
        }
        if (Double.isNaN(land.getLongitude())) {
            throw new IllegalStateException("Longitude not entered");
        }
        if (Double.isNaN(land.getLatitude())) {
            throw new IllegalStateException("Latitude not entered");
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
        Land savedLand = landRepo.save(land);
        return getDTO(savedLand);
    }

    public LandDTO updateUsageType(Integer id, String usageType) {
        Optional<Land> landOptional = landRepo.findById(id);
        if(!landOptional.isPresent()){
            throw new IllegalStateException("Land not found");
        }
        Land landToUpdate = landOptional.get();
        if(landToUpdate.getUsage_type() != null){
            landToUpdate.setUsage_type(usageType);
            Land updatedLand = landRepo.save(landToUpdate);
            return getDTO(updatedLand);
        }
        throw new IllegalStateException("Usage type not entered");
    }

    public LandDTO getLandRecordById(Integer id) {
        Optional<Land> landOptional = landRepo.findById(id);
        if(!landOptional.isPresent()){
            throw new IllegalStateException("Land not found");
        }
        Land land = landOptional.get();
        return getDTO(land);
    }

    public LandDTO deleteLandRecordById(Integer id) {
        Optional<Land> landOptional = landRepo.findById(id);
        if(!landOptional.isPresent()){
            throw new IllegalStateException("Land not found");
        }
        landRepo.deleteById(id);
        Land landToDelete = landOptional.get();
        return getDTO(landToDelete);
    }

    public List<LandDTO> getLandRecordsByUsageType(String usageType,String sortedBy) {
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
        if(lands.size() == 0){
            throw new IllegalStateException("No land found with the given usage type");
        }
        return getDTOList(lands);
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
                    land.getUsage_type()
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
            LandDTO dto = new LandDTO(land.getId(), land.getLocation(), land.getSurfaceArea(), land.getUsage_type());
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
            LandDTO dto = new LandDTO(land.getId(), land.getLocation(), land.getSurfaceArea(), land.getUsage_type());
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
            LandDTO dto = new LandDTO(land.getId(), land.getLocation(), land.getSurfaceArea(), land.getUsage_type());
            dto.setLocationCoordinates(land.getLatitude(), land.getLongitude());
            return dto;
        });
    }

    public List<LandDTO> getDTOList(List<Land> lands){
        List<LandDTO> landDTOList = new ArrayList<>();
        for(Land land : lands){
            LandDTO landDTO = new LandDTO(land.getId(),land.getLocation(),land.getSurfaceArea(),land.getUsage_type());
            landDTO.setLocationCoordinates(land.getLatitude(),land.getLongitude());
            landDTOList.add(landDTO);
        }
        return landDTOList;
    }

    public LandDTO getDTO(Land land){
        LandDTO landDTO = new LandDTO(land.getId(),land.getLocation(),land.getSurfaceArea(),land.getUsage_type());
        landDTO.setLocationCoordinates(land.getLatitude(),land.getLongitude());
        return landDTO;
    }
}
