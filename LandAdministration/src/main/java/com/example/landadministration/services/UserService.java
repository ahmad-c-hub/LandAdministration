package com.example.landadministration.services;


import com.example.landadministration.dtos.UsersDTO;
import com.example.landadministration.entities.Role;
import com.example.landadministration.entities.Users;
import com.example.landadministration.repos.RoleRepo;
import com.example.landadministration.repos.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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
            throw new IllegalStateException("Username is already taken!");
        }
        Role defaultRole = roleRepo.findByName("ROLE_USER")
                .orElseThrow(() -> new IllegalStateException("Default role not found"));

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
            UsersDTO usersDTO = new UsersDTO(user.getUsername(), user.getRole().getAuthority(),user.isGoogleUser());
            usersDTOList.add(usersDTO);
        }
        return usersDTOList;
    }

    public void setRole(Integer id, String role) {
        Users user = userRepo.findById(id)
                .orElseThrow(() -> new IllegalStateException("User not found"));
        Role roleObj = roleRepo.findByName(role)
                .orElseThrow(() -> new IllegalStateException("Role not found"));
        user.setRole(roleObj);
        userRepo.save(user);
    }

    public UsersDTO delete(Integer id) {
        Optional<Users> usersOptional = userRepo.findById(id);
        userRepo.deleteById(id);
        if(!usersOptional.isPresent()){
            throw new IllegalStateException("User not found");
        }
        Users userToDelete = usersOptional.get();
        UsersDTO userDTO = new UsersDTO(userToDelete.getUsername(), userToDelete.getRole().getAuthority(),userToDelete.isGoogleUser());
        return userDTO;
    }

    public UsersDTO updateUser(Integer id, Users user) {
        Optional<Users> usersOptional= userRepo.findById(id);
        if(!usersOptional.isPresent()){
                throw new IllegalStateException("User not found");
        }
        Users userToUpdate = usersOptional.get();
        if(user.getUsername() != null){
            userToUpdate.setUsername(user.getUsername());
        }
        if(user.getPassword()!=null){
            userToUpdate.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        }
        Users userUpdated = userRepo.save(userToUpdate);
        UsersDTO userDTO = new UsersDTO(userUpdated.getUsername(), userUpdated.getRole().getAuthority(),userUpdated.isGoogleUser());
        return userDTO;
    }
}
