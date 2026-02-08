package com.altspot.local.service;


import com.altspot.local.model.User;
import com.altspot.local.payload.SignupRequest;
import com.altspot.local.payload.UserDTO;
import org.springframework.security.core.Authentication;

public interface UserService {

    UserDTO registerUser(SignupRequest signupRequest);

    UserDTO getCurrentUser(Authentication authentication);
//    String verify (UserDTO user);
}
