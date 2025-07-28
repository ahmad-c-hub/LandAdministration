package com.example.landadministration.config;

import com.example.landadministration.entities.Role;
import com.example.landadministration.entities.UserLog;
import com.example.landadministration.entities.Users;
import com.example.landadministration.repos.RoleRepo;
import com.example.landadministration.repos.UserLogRepo;
import com.example.landadministration.repos.UserRepo;
import com.example.landadministration.services.JWTService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private  UserRepo usersRepository;

    @Autowired
    private UserLogRepo userLogRepo;

    @Autowired
    private RoleRepo roleRepo;

    @Autowired
    private  JWTService jwtService;


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");

        Role userRole = roleRepo.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Default role not found"));

        Optional<Users> optionalUser = usersRepository.findByUsername(email);
        Users user;

        if (optionalUser.isPresent()) {
            user = optionalUser.get();
            UserLog userLog = new UserLog();
            userLog.setUser(user);
            userLog.setAction("USER_LOGIN");
            userLog.setTimestamp(java.time.LocalDateTime.now());
            userLog.setDescription("User {"+ user.getUsername()+ "} logged in with google successfully.");
            userLogRepo.save(userLog);
        } else {
            user = new Users();
            user.setUsername(email);
            user.setPassword("");
            user.setEnabled(true);
            user.setRole(userRole);
            user.setIs_google_user(true);
            Users savedUser = usersRepository.save(user);
            UserLog userLog = new UserLog();
            userLog.setUser(savedUser);
            userLog.setAction("USER_REGISTRATION");
            userLog.setTimestamp(java.time.LocalDateTime.now());
            userLog.setDescription("New User {"+user.getUsername()+"} signed up with google successfully.");
            userLogRepo.save(userLog);
        }

        String jwtToken = jwtService.generateToken(user.getUsername());
        response.sendRedirect("https://ahmad-c-hub.github.io/LandAdministrationFRONTEND/oauth2/redirect?token=" + jwtToken);

    }
}
