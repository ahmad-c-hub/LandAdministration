package com.example.landadministration.services;


import com.example.landadministration.dtos.UsersDTO;
import com.example.landadministration.entities.Role;
import com.example.landadministration.entities.Users;
import com.example.landadministration.repos.RoleRepo;
import com.example.landadministration.repos.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private RoleRepo roleRepo;

    @Autowired
    private JWTService jwtService;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    public String verify(Users user) {
        Authentication authentication =
                authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getUsername(),user.getPassword()));

        if(authentication.isAuthenticated()){
            return jwtService.generateToken(user.getUsername());
        }
        return "fail";
    }

    public Users register(Users user) {
        if(userRepo.findByUsername(user.getUsername()).isPresent()){
            throw new IllegalArgumentException("Username is already taken!");
        }
        Role defaultRole = roleRepo.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Default role not found"));

        user.setRole(defaultRole);
        userRepo.save(user);
        user.setEnabled(true);
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        return userRepo.save(user);
    }

    public List<UsersDTO> getUsers() {
        List<Users> usersList = userRepo.findAll();
        List<UsersDTO> usersDTOList = new ArrayList<>();
        for(Users user : usersList){
            UsersDTO usersDTO = new UsersDTO(user.getUsername(), user.getRole().getAuthority());
            usersDTOList.add(usersDTO);
        }
        return usersDTOList;
    }

    public void setRole(Integer id, String role) {
        Users user = userRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Role roleObj = roleRepo.findByName(role)
                .orElseThrow(() -> new IllegalArgumentException("Role not found"));
        user.setRole(roleObj);
        userRepo.save(user);
    }
}
