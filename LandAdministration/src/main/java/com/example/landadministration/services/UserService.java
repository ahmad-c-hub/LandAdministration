package com.example.landadministration.services;


import com.example.landadministration.dtos.UsersDTO;
import com.example.landadministration.entities.Notification;
import com.example.landadministration.entities.Role;
import com.example.landadministration.entities.UserLog;
import com.example.landadministration.entities.Users;
import com.example.landadministration.repos.NotificationRepo;
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

    @Autowired
    private NotificationRepo notificationRepo;

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
        Page<Users> users;
        if(currentUser.getCountry().isEmpty()){
            users = userRepo.findAll(pageable);
            return users.map(user -> new UsersDTO(user.getUsername(), user.getRole().getAuthority(),user.isGoogleUser(),user.getCountry(),user.getId()));
        }else{
            users = userRepo.findAllByCountry(currentUser.getCountry(),pageable);
            return users.map(user -> new UsersDTO(user.getUsername(), user.getRole().getAuthority(),user.isGoogleUser(),user.getCountry(),user.getId()));
        }

    }

    public String setRole(Integer id, String role, Users userNavigating) {
        Users currentUser = (Users) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Users userToChange = userRepo.findById(id)
                .orElseThrow(() -> new IllegalStateException("User not found"));
        if(!currentUser.getCountry().isEmpty() && !userToChange.getCountry().equals(currentUser.getCountry())){
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

    public String requestRoleChange(Integer targetUserId, String roleName, String reason, Users countryAdmin) {
        Users targetUser = userRepo.findById(targetUserId)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        if (!countryAdmin.getCountry().equals(targetUser.getCountry())) {
            throw new IllegalStateException("User is not in your country");
        }

        Optional<Users> globalAdminOptional = userRepo.findById(1);
        Users globalAdmin = globalAdminOptional.orElseThrow(() -> new IllegalStateException("Global admin not found"));

        String message = "REQUEST: Promote user: " + targetUser.getUsername() +
                " to role: " + roleName + "\nREASON: " + reason;

        Notification notification = new Notification();
        notification.setSender(countryAdmin);
        notification.setReceiver(globalAdmin);
        notification.setTitle("Role Promotion Request");
        notification.setMessage(message);
        notification.setRead(false);
        notificationRepo.save(notification);

        return "Request sent to global admin.";
    }

    public String respondToRoleRequest(String responseMessage, Users globalAdmin) {
        String[] lines = responseMessage.split("\n");
        if (lines.length < 2) {
            return "Invalid message format. Please include a reason.";
        }

        String decisionLine = lines[0].trim(); // ACCEPTED or REJECTED line
        String reasonLine = lines[1].trim();   // REASON: something

        boolean approved = decisionLine.startsWith("ACCEPTED:");
        boolean rejected = decisionLine.startsWith("REJECTED:");
        if (!approved && !rejected) {
            return "Invalid decision format. Message must start with ACCEPTED: or REJECTED:";
        }

        try {
            // Example: "ACCEPTED: Promote user: adnan123 to role: COUNTRY_ADMIN"
            String usernamePart = decisionLine.split("user:")[1].split("to")[0].trim();
            String rolePart = decisionLine.split("role:")[1].trim();
            String reason = reasonLine.replace("REASON:", "").trim();

            Users userToChange = userRepo.findByUsername(usernamePart)
                    .orElseThrow(() -> new IllegalStateException("User not found"));

            Users originalRequester = notificationRepo
                    .findTopByMessageContainingAndReceiver_UsernameOrderByIssuedAtDesc("user: " + usernamePart, globalAdmin.getUsername())
                    .map(Notification::getSender)
                    .orElseThrow(() -> new IllegalStateException("Original requester not found"));

            if (approved) {
                Role newRole = roleRepo.findByName(rolePart)
                        .orElseThrow(() -> new IllegalStateException("Role not found"));

                if (!userToChange.getRole().equals(newRole)) {
                    userToChange.setRole(newRole);
                    userRepo.save(userToChange);
                }

                // Send notification to requester
                Notification confirmation = new Notification();
                confirmation.setSender(globalAdmin);
                confirmation.setReceiver(originalRequester);
                confirmation.setTitle("Role Change Approved");
                confirmation.setMessage("User '" + usernamePart + "' was promoted to '" + rolePart + "'.\nReason: " + reason);
                confirmation.setRead(false);
                notificationRepo.save(confirmation);

                return "Role change applied and requester notified.";

            } else if (rejected) {
                Notification rejection = new Notification();
                rejection.setSender(globalAdmin);
                rejection.setReceiver(originalRequester);
                rejection.setTitle("Role Change Rejected");
                rejection.setMessage("Role change request for user '" + usernamePart + "' to '" + rolePart + "' was rejected.\nReason: " + reason);
                rejection.setRead(false);
                notificationRepo.save(rejection);

                return "Rejection recorded and requester notified.";
            }

        } catch (Exception e) {
            return "Failed to process response: " + e.getMessage();
        }

        return "Unhandled response.";
    }





    public UsersDTO delete(Integer id, Users userNavigating) {
        Users currentUser = (Users) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(currentUser.getId().equals(id)){
            throw new IllegalStateException("You cannot delete yourself!");
        }
        Optional<Users> usersOptional = userRepo.findById(id);
        if(usersOptional.isPresent() && !usersOptional.get().getCountry().isEmpty() && !usersOptional.get().getCountry().equals(currentUser.getCountry())){
            throw new IllegalStateException("User is not in your country!");
        }
        userRepo.deleteById(id);
        if(usersOptional.isEmpty()){
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
        Users currentUser = (Users) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(currentUser.getCountry().isEmpty()){
            Optional<Users> usersOptional = userRepo.findById(id);
            if(usersOptional.isEmpty()){
                throw new IllegalStateException("User not found");
            }
            Users user = usersOptional.get();
            UsersDTO userDTO = new UsersDTO(user.getUsername(), user.getRole().getAuthority(),user.isGoogleUser(), user.getCountry(), user.getId());
            return userDTO;
        }else{
            Optional<Users> usersOptional = userRepo.findByIdAndCountry(id, currentUser.getCountry());
            if(!usersOptional.isPresent()){
                throw new IllegalStateException("User not found in your country.");
            }
            Users user = usersOptional.get();
            UsersDTO userDTO = new UsersDTO(user.getUsername(), user.getRole().getAuthority(),user.isGoogleUser(), user.getCountry(), user.getId());
            return userDTO;
        }
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
