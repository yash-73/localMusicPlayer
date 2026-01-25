package com.altspot.local.service;


import com.altspot.local.model.User;
import com.altspot.local.payload.UserDTO;

public interface UserService {

    UserDTO registerUser(UserDTO userDTO);
//    String verify (UserDTO user);
}
