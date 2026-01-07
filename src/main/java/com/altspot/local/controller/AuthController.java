package com.altspot.local.controller;


import com.altspot.local.payload.UserDTO;
import com.altspot.local.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/auth")
public class AuthController {

    private UserService userService;

   public AuthController(UserService userService) {
       this.userService = userService;
   }


    @PostMapping("/register")
    public ResponseEntity<UserDTO> registerUser(@RequestBody UserDTO userDTO) {
         UserDTO registeredUser = userService.registerUser(userDTO);
         return ResponseEntity.ok(registeredUser);
    }

}
