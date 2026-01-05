package com.altspot.local.service;

import com.altspot.local.exception.GeneralException;
import com.altspot.local.model.User;
import com.altspot.local.payload.UserDTO;
import com.altspot.local.repository.RoleRepository;
import com.altspot.local.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService{


    public UserRepository userRepository;
    public RoleRepository roleRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository,  RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public String registerUser(UserDTO userDTO) throws GeneralException {
        if(userDTO.getUsername().isEmpty()) throw new GeneralException("Username is empty");
        if(userDTO.getPassword().isEmpty()) throw new GeneralException("Password is empty");

        try{
            User user = new User();
            user.setUsername(userDTO.getUsername());
            user.setPassword(userDTO.getPassword());
            userDTO.getRoles().forEach(role -> {
                if(roleRepository.existsByName(role)){
                    user.getRoles().add(roleRepository.findByName(role));
                }
            });
        }
        catch (Exception e){
            throw new GeneralException(e.getMessage());
        }

        return "";
    }

}

