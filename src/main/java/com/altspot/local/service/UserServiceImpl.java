package com.altspot.local.service;

import com.altspot.local.config.userdetails.UserDetailsImpl;
import com.altspot.local.exception.GeneralException;
import com.altspot.local.exception.ResourceNotFound;
import com.altspot.local.model.AppRole;
import com.altspot.local.model.Role;
import com.altspot.local.model.User;
import com.altspot.local.payload.SignupRequest;
import com.altspot.local.payload.UserDTO;
import com.altspot.local.repository.RoleRepository;
import com.altspot.local.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService{


    private final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    public UserRepository userRepository;
    public RoleRepository roleRepository;
    public PasswordEncoder passwordEncoder;
//    public AuthenticationManager authenticationManager;

    @Autowired
    public UserServiceImpl(UserRepository userRepository,  RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
//        this.authenticationManager = authenticationManager;
    }

    @Override
    public UserDTO registerUser(SignupRequest signupRequest) throws GeneralException {
        if(signupRequest.getUsername().isEmpty()) throw new GeneralException("Username is empty");
        if(signupRequest.getPassword().isEmpty()) throw new GeneralException("Password is empty");

        try{
            Optional<User> user = userRepository.findByUsername(signupRequest.getUsername());
            if(user.isPresent()){throw new GeneralException("Username already exists");}
            else{
                User newUser = new User();

                newUser.setUsername(signupRequest.getUsername());

                newUser.setPassword(passwordEncoder.encode(signupRequest.getPassword()));

                if(signupRequest.getRoles() == null){
                    Set<Role> roles = new HashSet<>();
                    roles.add(roleRepository.findByName(AppRole.ROLE_USER));
                    newUser.setRoles(roles);
                }
                else{
                    signupRequest.getRoles().forEach(role -> {
                        AppRole appRole = AppRole.valueOf(role);
                        if(roleRepository.existsByName(appRole)){
                            newUser.getRoles().add(roleRepository.findByName(appRole));
                        }
                    });
                }

                userRepository.save(newUser);
                UserDTO newUserDTO = new UserDTO();
                Optional<User> savedUser = userRepository.findByUsername(signupRequest.getUsername());
                if(savedUser.isEmpty()){throw new GeneralException("User could not be saved");}
                else{
                    User saved =  savedUser.get();
                    newUserDTO.setId(newUser.getId());
                    newUserDTO.setUsername(saved.getUsername());
                    newUserDTO.setRoles(saved.getRoles().stream().map(Role::toString).collect(Collectors.toSet()));
                    return newUserDTO;
                }
            }

        }
        catch (Exception e){
            throw new GeneralException(e.getMessage());
        }
    }

    @Override
    public UserDTO getCurrentUser(Authentication authentication) {
        if(authentication == null) throw new GeneralException("Authentication is null");
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        if(userDetails == null) throw new ResourceNotFound("User not found");

        Optional<User> user = userRepository.findByUsername(userDetails.getUsername());
        if(user.isEmpty()) throw new ResourceNotFound("User not found after validation");
        User currentUser = user.get();

        UserDTO userDTO = new UserDTO();
        userDTO.setUsername(currentUser.getUsername());
        userDTO.setRoles(currentUser.getRoles().stream().map(Role::toString).collect(Collectors.toSet()));
        userDTO.setId(currentUser.getId());

        return userDTO;
    }
//


}

