package com.altspot.local.controller;


import com.altspot.local.model.User;
import com.altspot.local.payload.UserDTO;
import com.altspot.local.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/")
public class AuthController {

    private final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final UserService userService;


   public AuthController(UserService userService) {
       this.userService = userService;
   }

    @PostMapping("register")
    public ResponseEntity<UserDTO> registerUser(@RequestBody UserDTO userDTO) {
         UserDTO registeredUser = userService.registerUser(userDTO);
         return ResponseEntity.ok(registeredUser);
    }


}
