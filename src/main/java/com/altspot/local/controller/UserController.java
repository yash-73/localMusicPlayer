package com.altspot.local.controller;

import com.altspot.local.payload.UserDTO;
import com.altspot.local.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    public UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("")
    public String noHello(){return "No hello";}

    @GetMapping("hello")
    public String helloUser(){
        return "Hello there!";
    }

    @PostMapping("register")
    public ResponseEntity<Object> registerUser(UserDTO userDTO){
        try{
            String response = userService.registerUser(userDTO);
            return new ResponseEntity<>(userDTO, HttpStatus.OK);
        }
        catch(Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("sid")
    public ResponseEntity<String> getSessionId(HttpServletRequest request){
        String sessionId = request.getSession().getId();
        return ResponseEntity.ok(sessionId);
    }

    @PostMapping("post")
    public ResponseEntity<UserDTO> postData(@RequestBody UserDTO  userDTO){
        return ResponseEntity.ok(userDTO);
    }

}
