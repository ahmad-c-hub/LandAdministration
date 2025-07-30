package com.example.landadministration.services;

import com.example.landadministration.dtos.UserLogDTO;
import com.example.landadministration.entities.UserLog;
import com.example.landadministration.entities.Users;
import com.example.landadministration.repos.UserLogRepo;
import com.example.landadministration.repos.UserRepo;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserLogService {

    @Autowired
    private UserLogRepo userLogRepo;

    @Autowired
    private UserRepo userRepo;


    public Page<UserLogDTO> getUserLogRecords(int page, int size) {
        Users currentUser = (Users) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Pageable pageable = PageRequest.of(page,size);
        Page<UserLog> records = userLogRepo.findAllByOrderByCreatedAtDesc(pageable);
        if(currentUser.getCountry().isEmpty()){
            return records.map(record ->{
                String username = "Deleted User";
                String role = "N/A";
                if(record.getUser()!=null){
                    username = record.getUser().getUsername();
                    role = record.getUser().getRole().getAuthority();
                }
                UserLogDTO dto = new UserLogDTO(
                        username,
                        role,
                        record.getAction(),
                        record.getTimestamp(),
                        record.getDescription()
                );
                return dto;
            });
        }else{
            List<UserLog> recordsFiltered = new ArrayList<>();
            for(UserLog record : records){
                if(record.getUser()!=null && record.getUser().getCountry().equals(currentUser.getCountry())){
                    recordsFiltered.add(record);
                }
            }
            Page<UserLog> recordsPage = new PageImpl<>(recordsFiltered, pageable, recordsFiltered.size());
            return recordsPage.map(record ->{
                String username = "Deleted User";
                String role = "N/A";
                if(record.getUser()!=null){
                    username = record.getUser().getUsername();
                    role = record.getUser().getRole().getAuthority();
                }
                UserLogDTO dto = new UserLogDTO(
                        username,
                        role,
                        record.getAction(),
                        record.getTimestamp(),
                        record.getDescription()
                );
                return dto;
            });
        }

    }

    public Page<UserLogDTO> getUserLogRecordsByUserId(int page, int size, Integer id) {
        Users currentUser = (Users) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Pageable pageable = PageRequest.of(page,size);
        Optional<Users> user = userRepo.findById(id);
        if(!user.isPresent()){
            throw new IllegalStateException("User not found");
        }
        if(!currentUser.getCountry().equals(user.get().getCountry())){
            throw new IllegalStateException("User not in your country");
        }
        Page<UserLog> records = userLogRepo.findByUser_Id(id,pageable);
        return records.map(record ->{
            UserLogDTO dto = new UserLogDTO(
                    record.getUser().getUsername(),
                    record.getUser().getRole().getAuthority(),
                    record.getAction(),
                    record.getTimestamp(),
                    record.getDescription()
            );
            return dto;
        });

    }

    public Page<UserLogDTO> getCurrentUserLogRecords(int page, int size, Users userNavigating) {
        Pageable pageable = PageRequest.of(page,size);
        Page<UserLog> logs = userLogRepo.findByUsername(userNavigating.getUsername(),pageable);
        return logs.map(log ->{
            UserLogDTO dto = new UserLogDTO(
                    log.getUser().getUsername(),
                    log.getUser().getRole().getAuthority(),
                    log.getAction(),
                    log.getTimestamp(),
                    log.getDescription()
            );
            return dto;
        });
    }
}
