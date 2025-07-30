package com.example.landadministration.services;


import com.example.landadministration.dtos.UsersDTO;
import com.example.landadministration.entities.Role;
import com.example.landadministration.entities.UserLog;
import com.example.landadministration.entities.Users;
import com.example.landadministration.repos.RoleRepo;
import com.example.landadministration.repos.UserLogRepo;
import com.example.landadministration.repos.UserRepo;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
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
    private UserLogRepo userLogRepo;

    @Autowired
    private JWTService jwtService;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    public String verify(Users user) {
        try {
            Authentication authentication =
                    authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));

            if (authentication.isAuthenticated()) {
                UserLog userLog = new UserLog();
                Optional<Users> optionalUser = userRepo.findByUsername(user.getUsername());
                userLog.setUser(optionalUser.get());
                userLog.setAction("USER_LOGIN");
                userLog.setTimestamp(LocalDateTime.now());
                userLog.setDescription("User logged in successfully");
                userLogRepo.save(userLog);
                return jwtService.generateToken(user.getUsername());
            }
        } catch (Exception e) {
            System.out.println("Failed to login user: " + user.getUsername());
            Optional<Users> optionalUser = userRepo.findByUsername(user.getUsername());
            if (optionalUser.isPresent()) {
                UserLog userLog = new UserLog();
                userLog.setUser(optionalUser.get());
                userLog.setAction("USER_LOGIN");
                userLog.setTimestamp(LocalDateTime.now());
                userLog.setDescription("Failed to login user: {" + optionalUser.get().getUsername()+"}");
                userLogRepo.save(userLog);
            }
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
        user.setEnabled(true);
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        Users registeredUser = userRepo.save(user);
        UserLog userLog = new UserLog();
        userLog.setUser(registeredUser);
        userLog.setAction("USER_REGISTRATION");
        userLog.setTimestamp(LocalDateTime.now());
        userLog.setDescription("New User {"+user.getUsername()+"} signed up successfully.");
        userLogRepo.save(userLog);
        return registeredUser;
    }

    public Page<UsersDTO> getUsers(int page, int size) {
        Users currentUser = (Users) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        Pageable pageable = PageRequest.of(page,size, sort);
        Page<Users> users = userRepo.findAll(pageable);
        if(currentUser.getCountry().isEmpty()){
            return users.map(user -> new UsersDTO(user.getUsername(), user.getRole().getAuthority(),user.isGoogleUser(),user.getCountry(),user.getId()));
        }else{
            List<Users> usersFiltered = new ArrayList<>();
            for(Users user : users){
                if(user.getCountry()!=null && user.getCountry().equals(currentUser.getCountry())){
                    usersFiltered.add(user);
                }
            }
            Page<Users> usersPage = new PageImpl<>(usersFiltered, pageable, usersFiltered.size());
            return usersPage.map(user -> new UsersDTO(user.getUsername(), user.getRole().getAuthority(),user.isGoogleUser(),user.getCountry(),user.getId()));
        }

    }

    public String setRole(Integer id, String role, Users userNavigating) {
        Users currentUser = (Users) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Users userToChange = userRepo.findById(id)
                .orElseThrow(() -> new IllegalStateException("User not found"));
        if(userToChange.getCountry()!=null && !userToChange.getCountry().equals(currentUser.getCountry())){
            throw new IllegalStateException("User is not in your country!");
        }
        Role roleObj = roleRepo.findByName(role)
                .orElseThrow(() -> new IllegalStateException("Role not found"));
        if(userToChange.getRole().equals(roleObj)){
            throw new IllegalStateException("Role is already set");
        }
        userToChange.setRole(roleObj);
        userRepo.save(userToChange);
        UserLog userLog = new UserLog();
        userLog.setUser(userNavigating);
        userLog.setAction("ROLE_CHANGE");
        userLog.setTimestamp(LocalDateTime.now());
        userLog.setDescription("Admin "+userNavigating.getUsername()+" changed role of {"+
                userToChange.getUsername()+"} from {"+roleObj.getAuthority()+"} to {"+userToChange.getRole().getAuthority()+"} successfully.");
        userLogRepo.save(userLog);
        return "Role set successfully!";
    }

    public UsersDTO delete(Integer id, Users userNavigating) {
        Users currentUser = (Users) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<Users> usersOptional = userRepo.findById(id);
        if(usersOptional.isPresent() && usersOptional.get().getCountry()!=null && !usersOptional.get().getCountry().equals(currentUser.getCountry())){
            throw new IllegalStateException("User is not in your country!");
        }
        userRepo.deleteById(id);
        if(!usersOptional.isPresent()){
            throw new IllegalStateException("User not found");
        }
        Users userToDelete = usersOptional.get();
        UsersDTO userDTO = new UsersDTO(userToDelete.getUsername(), userToDelete.getRole().getAuthority(),userToDelete.isGoogleUser(), userToDelete.getCountry(),userToDelete.getId());
        UserLog userLog = new UserLog();
        userLog.setUser(userNavigating);
        userLog.setAction("Delete User");
        userLog.setTimestamp(LocalDateTime.now());
        userLog.setDescription("Admin {"+userNavigating.getUsername()+"} deleted user {"+userToDelete.getUsername()+"} successfully.");
        userLogRepo.save(userLog);
        return userDTO;
    }

    public UsersDTO getUserById(Integer id) {
        Optional<Users> usersOptional = userRepo.findById(id);
        if(!usersOptional.isPresent()){
            throw new IllegalStateException("User not found");
        }
        Users user = usersOptional.get();
        UsersDTO userDTO = new UsersDTO(user.getUsername(), user.getRole().getAuthority(),user.isGoogleUser(), user.getCountry(), user.getId());
        return userDTO;
    }

    public String logout(HttpServletRequest request, Users userNavigating) {
        String authHeader = request.getHeader("Authorization");
        System.out.println("Auth Header: "+authHeader);
        if(authHeader != null && authHeader.startsWith("Bearer ")){
            String token = authHeader.substring(7);
            jwtService.revokeToken(token);
            UserLog userLog = new UserLog();
            userLog.setUser(userNavigating);
            userLog.setAction("USER_LOGOUT");
            userLog.setTimestamp(LocalDateTime.now());
            userLog.setDescription("User {" + userNavigating.getUsername() + "} logged out successfully");
            userLogRepo.save(userLog);
            return "Logged out successfully! ";
        }
        return "No token found!";
    }

    public UsersDTO updateCurrentUser(Users userNavigating, Users userToUpdate) {
        Optional<Users> usersOptional = userRepo.findByUsername(userNavigating.getUsername());
        Users user =  usersOptional.get();
        String updatedUsername = userToUpdate.getUsername();
        if(updatedUsername != null &&!updatedUsername.equals(user.getUsername())){
            if(userRepo.findByUsername(updatedUsername).isPresent()){
                throw new IllegalStateException("Username is already taken!");
            }
            user.setUsername(updatedUsername);
            userRepo.save(user);
            UsersDTO userDTO = new UsersDTO(user.getUsername(), user.getRole().getAuthority(),user.isGoogleUser(), user.getCountry(), user.getId());
            UserLog userLog = new UserLog();
            userLog.setUser(userNavigating);
            userLog.setAction("PROFILE_CHANGE");
            userLog.setTimestamp(LocalDateTime.now());
            userLog.setDescription("User {"+userNavigating.getUsername()+"} commited a username change successfully.");
            userLogRepo.save(userLog);
            return userDTO;
        }else{
            return new UsersDTO(user.getUsername(), user.getRole().getAuthority(),user.isGoogleUser(), user.getCountry(), user.getId());
        }

    }

    public void updatePassword(Users user, String oldPassword, String newPassword) {
        if (oldPassword == null || newPassword == null || oldPassword.trim().isEmpty() || newPassword.trim().isEmpty()) {
            throw new IllegalStateException("Both old and new passwords are required.");
        }



        if (!bCryptPasswordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalStateException("Old password is incorrect.");
        }

        if (oldPassword.equals(newPassword)) {
            throw new IllegalStateException("New password cannot be the same as the old one.");
        }

        user.setPassword(bCryptPasswordEncoder.encode(newPassword));
        userRepo.save(user);

        // Optional: log action
        UserLog log = new UserLog();
        log.setUser(user);
        log.setAction("PROFILE_CHANGE");
        log.setTimestamp(LocalDateTime.now());
        log.setDescription("User {" + user.getUsername() + "} commited a password change successfully.");
        userLogRepo.save(log);
    }

}
