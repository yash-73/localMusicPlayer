package com.altspot.local.controller;


import com.altspot.local.payload.LoginRequest;
import com.altspot.local.payload.SignupRequest;
import com.altspot.local.payload.UserDTO;
import com.altspot.local.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/auth")
public class AuthController {

    private final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final UserService userService;


   public AuthController(UserService userService) {
       this.userService = userService;
   }

    @PostMapping("register")
    public ResponseEntity<UserDTO> registerUser(@RequestBody SignupRequest signupRequest) {
         UserDTO registeredUser = userService.registerUser(signupRequest);
         return ResponseEntity.ok(registeredUser);
    }

    @PostMapping("login")
    public void loginUser(HttpServletRequest request, @RequestBody LoginRequest loginRequest) throws ServletException {
            request.login(loginRequest.getUsername() , loginRequest.getPassword());
    }


    @GetMapping("isLoggedIn")
    public boolean isLoggedIn(Authentication authentication) {
       return (authentication != null && authentication.isAuthenticated());
    }

    @GetMapping("me")
    public ResponseEntity<UserDTO> getCurrentUser(Authentication authentication) {
       UserDTO userDTO = userService.getCurrentUser(authentication);
       return ResponseEntity.ok(userDTO);
    }


}
