package com.altspot.local.service;

import com.altspot.local.exception.GeneralException;
import com.altspot.local.model.AppRole;
import com.altspot.local.model.Role;
import com.altspot.local.model.User;
import com.altspot.local.payload.UserDTO;
import com.altspot.local.repository.RoleRepository;
import com.altspot.local.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService{

    Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    public UserRepository userRepository;
    public RoleRepository roleRepository;
    public PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository,  RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDTO registerUser(UserDTO userDTO) throws GeneralException {
        if(userDTO.getUsername().isEmpty()) throw new GeneralException("Username is empty");
        if(userDTO.getPassword().isEmpty()) throw new GeneralException("Password is empty");

        try{
            Optional<User> user = userRepository.findByUsername(userDTO.getUsername());
            if(user.isPresent()){throw new GeneralException("Username already exists");}
            else{
                User newUser = new User();

                newUser.setUsername(userDTO.getUsername());

                newUser.setPassword(passwordEncoder.encode(userDTO.getPassword()));

                userDTO.getRoles().forEach(role -> {
                    if(roleRepository.existsByName(role)){
                        newUser.getRoles().add(roleRepository.findByName(role));
                    }
                });

                userRepository.save(newUser);
                return userDTO;
            }

        }
        catch (Exception e){
            throw new GeneralException(e.getMessage());
        }
    }

}

